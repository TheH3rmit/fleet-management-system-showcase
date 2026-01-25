import { Component, inject, DestroyRef, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { TransportDTO } from "../../core/models/transport.model";
import { TransportService } from "../../core/services/transport/transport.service";
import { Page } from "../../core/models/page.model";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { TransportCreateDialogComponent } from "./transport-create-dialog/transport-create-dialog.component";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { TransportStatus } from "../../core/models/transport-status.model";
import { NotificationService } from '../../core/services/notification/notification.service';
import { TransportEditDialogComponent } from "./transport-edit-dialog/transport-edit-dialog.component";
import { AuthService } from '../../core/services/auth/auth.service';
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { ConfirmDialogService } from "../../shared/confirm-dialog.service";
import { DriverService } from "../../core/services/driver/driver.service";
import { VehicleService } from "../../core/services/vehicles/vehicle.service";
import { LocationService } from '../../core/services/location/location.service';
import { DriverDTO } from "../../core/models/driver.model";
import { VehicleDTO } from "../../core/models/vehicle.model";
import { LocationDTO } from "../../core/models/location.model";
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-transports-page',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatTableModule, MatPaginatorModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule,
    MatSnackBarModule, MatSortModule, MatDialogModule, MatProgressSpinner,
    MatTooltipModule,
  ],
  templateUrl: './transports-page.component.html',
  styleUrls: ['./transports-page.component.scss']
})
export class TransportsPageComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private transportService = inject(TransportService);
  private dialog = inject(MatDialog);
  private notify = inject(NotificationService);
  private auth = inject(AuthService);
  private confirm = inject(ConfirmDialogService);
  private driversApi = inject(DriverService);
  private vehiclesApi = inject(VehicleService);
  private locationApi = inject(LocationService);

  private driverMap = signal<Record<number, DriverDTO | null>>({});
  private vehicleMap = signal<Record<number, VehicleDTO | null>>({});
  private locationMap = signal<Record<number, LocationDTO | null>>({});
  isAdmin = computed(() => this.auth.hasRole('ADMIN'));
  isDispatcher = computed(() => this.auth.hasRole('DISPATCHER'));

  // Kolumny w tabeli
  cols = [
    'id',
    'driverId',
    'vehicleId',
    'status',
    'plannedStartAt',
    'plannedEndAt',
    'plannedDistanceKm',
    'pickupLocationId',
    'deliveryLocationId',
    'actions'
  ];

  // backendowa strona
  private pageState = signal<Page<TransportDTO> | null>(null);
  page = computed(() => this.pageState());

  // paginator
  pageIndex = signal(0);
  pageSize = signal(10);

  // prosty search (client-side)
  searchCtrl = new FormControl<string>('', { nonNullable: true });

  loading = false;

  ngOnInit() {
    this.searchCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.pageIndex.set(0);
        this.reload();
      });
    this.loadLocations();
    this.reload();
  }

  private parseFilters(qRaw: string) {
    const q = (qRaw ?? '').trim();
    if (!q) {
      return {
        status: undefined as TransportStatus | undefined,
        driverId: undefined as number | undefined,
        vehicleId: undefined as number | undefined,
        q: undefined as string | undefined
      };
    }

    const upper = q.toUpperCase();
    const normalizedStatus = upper.replace(/[\s-]+/g, '_');
    const statuses = Object.values(TransportStatus) as string[];
    if (statuses.includes(normalizedStatus)) {
      return { status: normalizedStatus as TransportStatus, driverId: undefined, vehicleId: undefined, q: undefined };
    }

    const pref = q.match(/^([a-z]+)\s*[:=]\s*(.+)$/i);
    if (pref) {
      const key = pref[1].toLowerCase();
      const value = pref[2].trim();
      const num = Number(value);
      const isNum = Number.isFinite(num) && value.match(/^\d+$/);

      if (['d', 'driver', 'driverid'].includes(key)) {
        if (isNum) return { status: undefined, driverId: num, vehicleId: undefined, q: undefined };
        return { status: undefined, driverId: undefined, vehicleId: undefined, q: value };
      }
      if (['v', 'vehicle', 'vehicleid'].includes(key)) {
        if (isNum) return { status: undefined, driverId: undefined, vehicleId: num, q: undefined };
        return { status: undefined, driverId: undefined, vehicleId: undefined, q: value };
      }
      if (['s', 'status'].includes(key)) {
        const normalizedValue = value.toUpperCase().replace(/[\s-]+/g, '_');
        if (statuses.includes(normalizedValue)) {
          return { status: normalizedValue as TransportStatus, driverId: undefined, vehicleId: undefined, q: undefined };
        }
        return { status: undefined, driverId: undefined, vehicleId: undefined, q: value };
      }
    }

    if (q.match(/^\d+$/)) {
      return { status: undefined, driverId: undefined, vehicleId: undefined, q };
    }

    return { status: undefined, driverId: undefined, vehicleId: undefined, q };
  }

  reload() {
    this.loading = true;

    const { status, driverId, vehicleId, q } = this.parseFilters(this.searchCtrl.value);

    this.transportService
      .list({
        page: this.pageIndex(),
        size: this.pageSize(),
        status,
        driverId,
        vehicleId,
        q
      })
      .subscribe({
        next: (p) => {
          this.pageState.set(p);
          this.loadRefsForPage(p.content ?? []);
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  onPage(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.reload();
  }

  onSort(e: Sort) {
    if (!e.direction || !this.page()) return;

    const key = e.active as keyof TransportDTO;
    const dir = e.direction === 'asc' ? 1 : -1;

    const sorted = [...this.page()!.content].sort((a, b) => {
      const av = a[key] ?? '';
      const bv = b[key] ?? '';
      if (av === bv) return 0;
      return av > bv ? dir : -dir;
    });

    this.pageState.set({ ...this.page()!, content: sorted });
  }

  openCreateDialog() {
    const ref = this.dialog.open(TransportCreateDialogComponent, {
      width: '600px',
      panelClass: 'safe-dialog',
    });

    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => {
        if (res === 'ok') {
          this.notify.success('Created');
          this.reload();
        }
      });
  }

  openEditDialog(t: TransportDTO) {
    const ref = this.dialog.open(TransportEditDialogComponent, {
      width: '720px',
      panelClass: 'safe-dialog',
      data: { transport: t }
    });

    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(res => {
        if (res === 'ok') this.reload();
      });
  }

  deleteTransport(t: TransportDTO) {
    if (!(this.isAdmin() || this.isDispatcher())) return;

    this.confirm.confirm({
      title: 'Delete transport',
      message: `Are you sure you want to delete transport #${t.id}?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      icon: 'delete_forever',
      confirmColor: 'warn',
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(ok => {
        if (!ok) return;

        this.transportService.delete(t.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.notify.success('Deleted');
              this.reload();
            },
            error: (err: any) =>
              this.notify.error(err?.userMessage ?? 'Failed to delete transport'),
          });
      });
  }

  canEdit(t: TransportDTO): boolean {
    return t.status === TransportStatus.PLANNED;
  }

  canDelete(t: TransportDTO): boolean {
    return (this.isAdmin() || this.isDispatcher()) && t.status === TransportStatus.PLANNED;
  }

  editTooltip(t: TransportDTO): string {
    if (!this.canEdit(t)) {
      return 'Cannot edit: only PLANNED transports are editable.';
    }
    return 'Edit transport';
  }

  deleteTooltip(t: TransportDTO): string {
    if (!(this.isAdmin() || this.isDispatcher())) return 'Cannot delete: only admin/dispatcher can delete transports.';
    if (t.status !== TransportStatus.PLANNED) {
      return 'Cannot delete: only PLANNED transports can be deleted.';
    }
    return 'Delete transport';
  }

  driverLabel(driverId?: number | null) {
    if (!driverId) return 'N/A';
    const d = this.driverMap()[driverId];
    if (!d) return `#${driverId}`; // fallback
    return `${d.firstName} ${d.lastName}`;
  }

  vehicleLabel(vehicleId?: number | null) {
    if (!vehicleId) return 'N/A';
    const v = this.vehicleMap()[vehicleId];
    if (!v) return `#${vehicleId}`;
    return `${v.licensePlate} - ${v.manufacturer} ${v.model}`;
  }

  locationLabel(locationId?: number | null) {
    if (!locationId) return 'N/A';
    const l = this.locationMap()[locationId];
    if (!l) return `#${locationId}`;

    const street = [l.street, l.buildingNumber].filter(Boolean).join(' ');
    const cityLine = [l.postcode, l.city].filter(Boolean).join(' ');
    const parts = [street, cityLine, l.country].filter(Boolean);
    return parts.join(', ');
  }

  private loadRefsForPage(list: TransportDTO[]) {
    const neededDriverIds = Array.from(new Set(
      list.map(t => t.driverId).filter((x): x is number => !!x)
    ));

    const neededVehicleIds = Array.from(new Set(
      list.map(t => t.vehicleId).filter((x): x is number => !!x)
    ));

    const currentDriverMap = this.driverMap();
    const currentVehicleMap = this.vehicleMap();

    const missingDriverIds = neededDriverIds.filter(id => !currentDriverMap[id]);
    const missingVehicleIds = neededVehicleIds.filter(id => !currentVehicleMap[id]);

    if (missingDriverIds.length) {
      this.driversApi.getManyByIds(missingDriverIds)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (drivers) => {
            const mapD = { ...this.driverMap() };
            for (const d of (drivers ?? [])) mapD[d.userId] = d;
            this.driverMap.set(mapD);
          },
          error: () => { /* fallback in driverLabel */ }
        });
    }

    if (missingVehicleIds.length) {
      this.vehiclesApi.getManyByIds(missingVehicleIds)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (vehicles) => {
            const mapV = { ...this.vehicleMap() };
            for (const v of (vehicles ?? [])) mapV[v.id] = v;
            this.vehicleMap.set(mapV);
          },
          error: () => { /* fallback */ }
        });
    }
  }

  private loadLocations() {
    this.locationApi.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (locations) => {
          const mapL: Record<number, LocationDTO | null> = {};
          for (const l of (locations ?? [])) mapL[l.id] = l;
          this.locationMap.set(mapL);
        },
        error: () => { /* fallback */ }
      });
  }
}



