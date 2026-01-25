import { Component, DestroyRef, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { VehicleService } from '../../core/services/vehicles/vehicle.service';
import { TrailerService } from '../../core/services/trailer/trailer.service';
import { NotificationService } from '../../core/services/notification/notification.service';

import { VehicleDTO } from '../../core/models/vehicle.model';
import { TrailerDTO } from '../../core/models/trailer.model';

import { VehicleCreateDialogComponent } from './vehicle-create-dialog/vehicle-create-dialog.component';
import { TrailerCreateDialogComponent } from './trailer-create-dialog/trailer-create-dialog.component';
import { VehicleEditDialogComponent } from './vehicle-edit-dialog/vehicle-edit-dialog.component';
import { TrailerEditDialogComponent } from './trailer-edit-dialog/trailer-edit-dialog.component';
import { MatTooltip } from '@angular/material/tooltip';
import { MatSortModule, Sort } from '@angular/material/sort';
import { ConfirmDialogService } from '../../shared/confirm-dialog.service';
import { Page } from '../../core/models/page.model';

@Component({
  selector: 'app-assets-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,

    MatTabsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltip,
    MatSortModule,
  ],
  templateUrl: './assets-page.component.html',
  styleUrl: './assets-page.component.scss'
})
export class AssetsPageComponent implements OnInit {
  private vehiclesApi = inject(VehicleService);
  private trailersApi = inject(TrailerService);
  private notify = inject(NotificationService);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);
  private confirm = inject(ConfirmDialogService);

  tabIndex = signal(0);

  loadingVehicles = signal(false);
  loadingTrailers = signal(false);

  vehiclePage = signal<Page<VehicleDTO>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
  });

  trailerPage = signal<Page<TrailerDTO>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
  });

  vehicleSort = signal<Sort | null>(null);
  trailerSort = signal<Sort | null>(null);

  searchCtrl = new FormControl<string>('', { nonNullable: true });
  private searchQuery = signal('');

  pageIndex = signal(0);
  pageSize = signal(10);

  vehicleCols = ['id', 'manufacturer', 'model', 'licensePlate', 'status', 'inTransport', 'actions'];
  trailerCols = ['id', 'name', 'licensePlate', 'payload', 'volume', 'status', 'inTransport', 'actions'];

  vehicles = computed(() => this.vehiclePage().content ?? []);
  trailers = computed(() => this.trailerPage().content ?? []);

  total = computed(() => {
    return this.tabIndex() === 0 ? this.vehiclePage().totalElements : this.trailerPage().totalElements;
  });

  ngOnInit() {
    this.loadVehicles();
    this.loadTrailers();

    this.searchQuery.set(this.searchCtrl.value);
    this.searchCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.searchQuery.set(value ?? '');
        this.pageIndex.set(0);
        if (this.tabIndex() === 0) this.loadVehicles();
        else this.loadTrailers();
      });
  }

  loadVehicles() {
    this.loadingVehicles.set(true);
    this.vehiclesApi.list({
      q: this.searchQuery(),
      page: this.pageIndex(),
      size: this.pageSize(),
      sort: this.buildVehicleSortParams(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.vehiclePage.set(page);
          this.loadingVehicles.set(false);
        },
        error: (err: any) => {
          this.loadingVehicles.set(false);
          this.notify.error(err?.userMessage ?? 'Failed to load vehicles');
        }
      });
  }

  loadTrailers() {
    this.loadingTrailers.set(true);
    this.trailersApi.list({
      q: this.searchQuery(),
      page: this.pageIndex(),
      size: this.pageSize(),
      sort: this.buildTrailerSortParams(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.trailerPage.set(page);
          this.loadingTrailers.set(false);
        },
        error: (err: any) => {
          this.loadingTrailers.set(false);
          this.notify.error(err?.userMessage ?? 'Failed to load trailers');
        }
      });
  }

  private buildVehicleSortParams(): string[] | undefined {
    const s = this.vehicleSort();
    if (!s) return undefined;
    return [`${s.active},${s.direction}`];
  }

  private buildTrailerSortParams(): string[] | undefined {
    const s = this.trailerSort();
    if (!s) return undefined;
    return [`${s.active},${s.direction}`];
  }

  onTabChange(index: number) {
    this.tabIndex.set(index);
    this.pageIndex.set(0);
    if (index === 0) {
      this.trailerSort.set(null);
      this.loadVehicles();
    } else {
      this.vehicleSort.set(null);
      this.loadTrailers();
    }
  }

  onPage(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    if (this.tabIndex() === 0) this.loadVehicles();
    else this.loadTrailers();
  }

  onSortVehicles(e: Sort) {
    this.vehicleSort.set(e.direction ? e : null);
    this.pageIndex.set(0);
    this.loadVehicles();
  }

  onSortTrailers(e: Sort) {
    this.trailerSort.set(e.direction ? e : null);
    this.pageIndex.set(0);
    this.loadTrailers();
  }

  openAddVehicle() {
    const ref = this.dialog.open(VehicleCreateDialogComponent, { width: '720px', panelClass: 'safe-dialog' });
    ref.afterClosed().subscribe(res => { if (res === 'ok') this.loadVehicles(); });
  }

  openAddTrailer() {
    const ref = this.dialog.open(TrailerCreateDialogComponent, { width: '720px', panelClass: 'safe-dialog' });
    ref.afterClosed().subscribe(res => { if (res === 'ok') this.loadTrailers(); });
  }

  assetUsageLabel(assigned?: boolean, inProgress?: boolean): string {
    if (inProgress) return 'Active transport';
    if (assigned) return 'Assigned';
    return 'No';
  }

  openEditVehicle(v: VehicleDTO) {
    const ref = this.dialog.open(VehicleEditDialogComponent,
      { width: '720px',
        panelClass: 'safe-dialog',
        data: { vehicle: v } });
    ref.afterClosed().subscribe(res => { if (res === 'ok') this.loadVehicles(); });
  }

  openEditTrailer(t: TrailerDTO) {
    const ref = this.dialog.open(TrailerEditDialogComponent,
      { width: '720px',
        panelClass: 'safe-dialog',
        data: { trailer: t } });
    ref.afterClosed().subscribe(res => { if (res === 'ok') this.loadTrailers(); });
  }

  deleteVehicle(v: VehicleDTO) {
    if (!this.canDeleteVehicle(v)) {
      this.notify.error(this.deleteVehicleTooltip(v));
      return;
    }
    this.confirm.confirm({
      title: 'Delete vehicle',
      message: `Delete vehicle ${v.licensePlate}?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      icon: 'delete_forever',
      confirmColor: 'warn',
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(ok => {
        if (!ok) return;

        this.vehiclesApi.deleteVehicle(v.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => { this.notify.success('Vehicle deleted'); this.loadVehicles(); },
            error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to delete vehicle'),
          });
      });
  }

  deleteTrailer(t: TrailerDTO) {
    if (!this.canDeleteTrailer(t)) {
      this.notify.error(this.deleteTrailerTooltip(t));
      return;
    }
    this.confirm.confirm({
      title: 'Delete trailer',
      message: `Delete trailer ${t.licensePlate}?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      icon: 'delete_forever',
      confirmColor: 'warn',
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(ok => {
        if (!ok) return;

        this.trailersApi.deleteTrailer(t.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => { this.notify.success('Trailer deleted'); this.loadTrailers(); },
            error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to delete trailer'),
          });
      });
  }

  canDeleteVehicle(v: VehicleDTO): boolean {
    return !v.assignedToTransport;
  }

  deleteVehicleTooltip(v: VehicleDTO): string {
    return v.assignedToTransport
      ? 'Cannot delete: vehicle is assigned to a transport.'
      : 'Delete vehicle';
  }

  canDeleteTrailer(t: TrailerDTO): boolean {
    return !t.assignedToTransport;
  }

  deleteTrailerTooltip(t: TrailerDTO): string {
    return t.assignedToTransport
      ? 'Cannot delete: trailer is assigned to a transport.'
      : 'Delete trailer';
  }
}



