import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { LocationDTO } from '../../../core/models/location.model';
import { LocationService } from '../../../core/services/location/location.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { ConfirmDialogService } from '../../../shared/confirm-dialog.service';

import { LocationCreateDialogComponent } from '../location-create-dialog/location-create-dialog.component';
import { LocationEditDialogComponent } from '../location-edit-dialog/location-edit-dialog.component';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { Page } from '../../../core/models/page.model';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-locations-manage-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatPaginator,
    MatSortModule,
    MatTooltipModule,
  ],
  templateUrl: './location-manage-page.component.html',
  styleUrl: './location-manage-page.component.scss',
})
export class LocationManagePageComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private api = inject(LocationService);
  private notify = inject(NotificationService);
  private dialog = inject(MatDialog);
  private confirm = inject(ConfirmDialogService);

  cols = ['id', 'address', 'postcode', 'country', 'coords', 'actions'];

  loading = signal(false);
  sortState = signal<Sort | null>(null);

  page = signal<Page<LocationDTO>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
  });

  searchCtrl = new FormControl<string>('', { nonNullable: true });
  private searchQuery = signal('');

  pageIndex = signal(0);
  pageSize = signal(10);

  ngOnInit(): void {
    this.load();

    this.searchQuery.set(this.searchCtrl.value);

    this.searchCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.searchQuery.set(value ?? '');
        this.pageIndex.set(0);
        this.load();
      });
  }

  load() {
    this.loading.set(true);

    this.api.list({
      q: this.searchQuery(),
      page: this.pageIndex(),
      size: this.pageSize(),
      sort: this.buildSortParams(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: (err: any) => {
          this.loading.set(false);
          this.notify.error(err?.userMessage ?? 'Failed to load locations');
        }
      });
  }

  onSort(e: Sort) {
    this.sortState.set(e.direction ? e : null);
    this.pageIndex.set(0);
    this.load();
  }

  onPage(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.load();
  }

  private buildSortParams(): string[] | undefined {
    const s = this.sortState();
    if (!s) return undefined;

    const dir = s.direction;
    switch (s.active) {
      case 'address':
        return [`city,${dir}`, `street,${dir}`, `buildingNumber,${dir}`];
      case 'coords':
        return [`latitude,${dir}`, `longitude,${dir}`];
      default:
        return [`${s.active},${dir}`];
    }
  }

  addressLabel(l: LocationDTO) {
    return `${l.city ?? ''}, ${l.street ?? ''} ${l.buildingNumber ?? ''}`.replace(/\s+/g, ' ').trim();
  }

  coordsLabel(l: LocationDTO) {
    if (l.latitude == null || l.longitude == null) return 'N/A';
    return `${l.latitude}, ${l.longitude}`;
  }

  openCreate() {
    const ref = this.dialog.open(LocationCreateDialogComponent, {
      width: '720px',
      panelClass: 'safe-dialog',
      data: { mode: 'create', title: 'Add location' },
    });

    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((created: LocationDTO | null) => {
        if (!created) return;
        this.pageIndex.set(0);
        this.load();
      });
  }

  openEdit(l: LocationDTO) {
    const ref = this.dialog.open(LocationEditDialogComponent, {
      width: '720px',
      panelClass: 'safe-dialog',
      data: { mode: 'edit', location: l, title: `Edit location #${l.id}` },
    });

    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((updated: LocationDTO | null) => {
        if (!updated) return;
        this.load();
      });
  }

  remove(l: LocationDTO) {
    if (!this.canDelete(l)) {
      this.notify.error(this.deleteTooltip(l));
      return;
    }
    this.confirm.confirm({
      title: 'Delete location',
      message: `Delete location #${l.id} (${this.addressLabel(l)})?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      icon: 'delete_forever',
      confirmColor: 'warn',
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(ok => {
        if (!ok) return;

        this.api.delete(l.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.notify.success('Location deleted');
              this.load();
            },
            error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to delete location'),
          });
      });
  }

  canDelete(l: LocationDTO): boolean {
    return !l.usedInTransport;
  }

  deleteTooltip(l: LocationDTO): string {
    if (l.usedAsPickup && l.usedAsDelivery) {
      return 'Cannot delete: location is used as pickup and delivery.';
    }
    if (l.usedAsPickup) {
      return 'Cannot delete: location is used as pickup.';
    }
    if (l.usedAsDelivery) {
      return 'Cannot delete: location is used as delivery.';
    }
    return 'Delete location';
  }
}



