import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { CargoService } from '../../../core/services/cargo/cargo.service';
import { CargoDTO } from '../../../core/models/cargo.model';
import { TransportStatus } from '../../../core/models/transport-status.model';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { ConfirmDialogService } from '../../../shared/confirm-dialog.service';
import { CargoEditDialogComponent } from '../cargo-edit-dialog/cargo-edit-dialog.component';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { Page } from '../../../core/models/page.model';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-cargos-manage',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinner,
    MatPaginatorModule,
    MatSortModule,
    MatTooltipModule,
  ],
  templateUrl: './cargos-manage.component.html',
  styleUrls: ['./cargos-manage.component.scss'],
})
export class CargosManageComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private api = inject(CargoService);
  private dialog = inject(MatDialog);
  private notify = inject(NotificationService);
  private confirm = inject(ConfirmDialogService);

  page = signal<Page<CargoDTO>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 10,
    number: 0,
  });
  loading = signal(false);

  searchCtrl = new FormControl<string>('', { nonNullable: true });
  private searchQuery = signal('');

  pageIndex = signal(0);
  pageSize = signal(10);

  sortState = signal<Sort | null>(null);

  cols = ['id', 'transportId', 'cargoDescription', 'weightKg', 'volumeM3', 'pickupDate', 'deliveryDate', 'actions'];

  rows = computed(() => this.page().content ?? []);
  total = computed(() => this.page().totalElements ?? 0);

  ngOnInit() {
    this.searchQuery.set(this.searchCtrl.value);

    this.searchCtrl.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.searchQuery.set(value ?? '');
        this.pageIndex.set(0);
        this.load();
      });

    this.load();
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
        error: () => {
          this.loading.set(false);
          this.notify.error('Failed to load cargos');
        }
      });
  }

  onPage(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.load();
  }

  onSort(e: Sort) {
    this.sortState.set(e.direction ? e : null);
    this.pageIndex.set(0);
    this.load();
  }

  private buildSortParams(): string[] | undefined {
    const s = this.sortState();
    if (!s) return undefined;

    const dir = s.direction;
    if (s.active === 'transportId') return [`transport.id,${dir}`];
    return [`${s.active},${dir}`];
  }

  openCreate() {
    this.dialog.open(CargoEditDialogComponent, {
      width: '520px',
      panelClass: 'safe-dialog',
      data: { mode: 'create' as const }
    }).afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((ok?: boolean) => {
        if (ok) this.load();
      });
  }

  openEdit(c: CargoDTO) {
    this.dialog.open(CargoEditDialogComponent, {
      width: '520px',
      panelClass: 'safe-dialog',
      data: { mode: 'edit' as const, cargo: c }
    }).afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((ok?: boolean) => {
        if (ok) this.load();
      });
  }

  delete(c: CargoDTO) {
    if (!this.canDelete(c)) {
      this.notify.error(this.deleteTooltip(c));
      return;
    }
    this.confirm.confirm({
      title: 'Delete cargo',
      message: `Delete cargo #${c.id}?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      icon: 'delete_forever',
      confirmColor: 'warn',
    }).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(ok => {
        if (!ok) return;

        this.api.delete(c.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.notify.success('Deleted');
              this.load();
            },
            error: (err) => this.notify.error(err?.userMessage ?? err?.error?.message ?? 'Failed to delete cargo')
          });
      });
  }

  canDelete(c: CargoDTO): boolean {
    return !c.transportStatus || c.transportStatus === TransportStatus.PLANNED;
  }

  deleteTooltip(c: CargoDTO): string {
    if (c.transportStatus && c.transportStatus !== TransportStatus.PLANNED) {
      return `Cannot delete: transport status is ${c.transportStatus}.`;
    }
    return 'Delete cargo';
  }
}



