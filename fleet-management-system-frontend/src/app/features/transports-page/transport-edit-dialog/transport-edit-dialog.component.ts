import { Component, DestroyRef, inject, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';

import { TransportService } from '../../../core/services/transport/transport.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { NotificationService } from '../../../core/services/notification/notification.service';

import { VehicleService } from '../../../core/services/vehicles/vehicle.service';
import { TrailerService } from '../../../core/services/trailer/trailer.service';
import { LocationService } from '../../../core/services/location/location.service';
import { DriverService } from '../../../core/services/driver/driver.service';

import { TransportDTO, CreateTransportRequest } from '../../../core/models/transport.model';
import { TransportStatus } from '../../../core/models/transport-status.model';
import { VehicleDTO } from '../../../core/models/vehicle.model';
import { TrailerDTO } from '../../../core/models/trailer.model';
import { LocationDTO } from '../../../core/models/location.model';
import { DriverDTO } from '../../../core/models/driver.model';
import { DriverStatus } from '../../../core/models/driver-status.model';
import { VehicleStatus } from '../../../core/models/vehicle-status.model';
import { TrailerStatus } from '../../../core/models/trailer-status.model';
import { StatusHistoryDialogComponent } from "../status-history-dialog/status-history-dialog.component";
import { MatIconModule } from "@angular/material/icon";

@Component({
  selector: 'app-transport-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule
  ],
  templateUrl: './transport-edit-dialog.component.html',
  styleUrl: './transport-edit-dialog.component.scss'
})
export class TransportEditDialogComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<TransportEditDialogComponent>);
  private api = inject(TransportService);
  private auth = inject(AuthService);
  private notify = inject(NotificationService);

  private vehicleApi  = inject(VehicleService);
  private trailerApi  = inject(TrailerService);
  private locationApi = inject(LocationService);
  private driverApi   = inject(DriverService);
  private dialog = inject(MatDialog);

  data = inject(MAT_DIALOG_DATA) as { transport: TransportDTO };

  loadingLists = signal(false);

  vehicles: VehicleDTO[] = [];
  trailers: TrailerDTO[] = [];
  locations: LocationDTO[] = [];
  drivers: DriverDTO[] = [];

  isAdmin = computed(() => this.auth.hasRole('ADMIN'));
  isDispatcher = computed(() => this.auth.hasRole('DISPATCHER'));
  isPlanned = computed(() => this.data.transport.status === TransportStatus.PLANNED);
  canEdit = computed(() => this.isPlanned());

  form = this.fb.group({
    contractualDueAt: [''],
    plannedStartAt: ['', [Validators.required]],
    plannedEndAt: [''],
    plannedDistanceKm: [null as number | null, [Validators.min(0.01)]],
    trailerId: [null as number | null, [Validators.required]],
    vehicleId: [null as number | null, [Validators.required]],
    pickupLocationId: [null as number | null, [Validators.required]],
    deliveryLocationId: [null as number | null, [Validators.required]],
    driverId: [null as number | null],
  });

  ngOnInit(): void {
    this.patchFromTransport();
    this.loadSelectData();

    if (!this.canEdit()) {
      this.form.disable();
    }
  }

  private patchFromTransport() {
    const t = this.data.transport;

    const toLocal = (iso?: string | null) => {
      if (!iso) return '';
      return iso.substring(0, 16);
    };

    this.form.patchValue({
      contractualDueAt: toLocal((t as any).contractualDueAt ?? null),
      plannedStartAt: toLocal((t as any).plannedStartAt ?? null),
      plannedEndAt: toLocal((t as any).plannedEndAt ?? null),
      plannedDistanceKm: (t as any).plannedDistanceKm ?? null,
      trailerId: (t as any).trailerId ?? null,
      vehicleId: (t as any).vehicleId ?? null,
      pickupLocationId: (t as any).pickupLocationId ?? null,
      deliveryLocationId: (t as any).deliveryLocationId ?? null,
      driverId: (t as any).driverId ?? null,
    });
  }

  private loadSelectData() {
    this.loadingLists.set(true);

    forkJoin({
      vehicles: this.vehicleApi.getAll(),
      trailers: this.trailerApi.getAll(),
      locations: this.locationApi.getAll(),
      drivers: this.driverApi.getAll(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ vehicles, trailers, locations, drivers }) => {
          this.vehicles = vehicles ?? [];
          this.trailers = trailers ?? [];
          this.locations = locations ?? [];
          this.drivers = (drivers ?? []).sort((a: DriverDTO, b: DriverDTO) =>
            (a.lastName + a.firstName).localeCompare(b.lastName + b.firstName)
          );
        },
        error: () => this.notify.error('Failed to load form data'),
        complete: () => this.loadingLists.set(false),
      });
  }

  availableDrivers(): DriverDTO[] {
    const current = this.form.value.driverId;
    return (this.drivers ?? []).filter(d =>
      d.driverStatus === DriverStatus.AVAILABLE || d.userId === current
    );
  }

  availableVehicles(): VehicleDTO[] {
    const current = this.form.value.vehicleId;
    return (this.vehicles ?? []).filter(v =>
      v.vehicleStatus === VehicleStatus.ACTIVE || v.id === current
    );
  }

  availableTrailers(): TrailerDTO[] {
    const current = this.form.value.trailerId;
    return (this.trailers ?? []).filter(t =>
      t.trailerStatus === TrailerStatus.ACTIVE || t.id === current
    );
  }

  submit() {
    if (!this.canEdit()) {
      this.notify.warn('Only PLANNED transports can be edited');
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.value;
    const startAt = v.plannedStartAt ? new Date(v.plannedStartAt).getTime() : null;
    const endAt = v.plannedEndAt ? new Date(v.plannedEndAt).getTime() : null;
    if (startAt && endAt && endAt < startAt) {
      this.notify.warn('Planned end cannot be before planned start.');
      return;
    }
    const toIso = (value?: string | null) => (value ? new Date(value).toISOString() : null);

    const dto: CreateTransportRequest = {
      contractualDueAt: toIso(v.contractualDueAt),
      plannedStartAt: toIso(v.plannedStartAt)!,
      plannedEndAt: toIso(v.plannedEndAt),
      plannedDistanceKm: v.plannedDistanceKm ?? null,
      trailerId: v.trailerId!,
      vehicleId: v.vehicleId!,
      pickupLocationId: v.pickupLocationId!,
      deliveryLocationId: v.deliveryLocationId!,
      driverId: v.driverId ?? null,
    };

    const id = this.data.transport.id;

    this.api.update(id, dto)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.notify.success('Transport updated'); this.ref.close('ok'); },
        error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to update transport'),
      });
  }

  openStatusHistory() {
    this.dialog.open(StatusHistoryDialogComponent, {
      width: '760px',
      panelClass: 'safe-dialog',
      data: { transportId: this.data.transport.id }
    });
  }

  close() { this.ref.close(); }
}



