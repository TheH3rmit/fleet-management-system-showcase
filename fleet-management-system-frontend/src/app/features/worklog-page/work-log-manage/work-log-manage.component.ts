import { Component, DestroyRef, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { DriverWorkLogService } from '../../../core/services/driver-work-log/driver-work-log.service';
import { DriverWorkLogDTO } from '../../../core/models/driver-work-log.model';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { ConfirmDialogService } from '../../../shared/confirm-dialog.service';
import { WorkLogFormDialogComponent } from '../work-log-form-dialog/work-log-form-dialog.component';
import { AuthService } from '../../../core/services/auth/auth.service';



@Component({
  selector: 'app-work-log-manage',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './work-log-manage.component.html',
  styleUrls: ['./work-log-manage.component.scss'],
})
export class WorkLogManageComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private api = inject(DriverWorkLogService);
  private notify = inject(NotificationService);
  private confirm = inject(ConfirmDialogService);
  private dialog = inject(MatDialog);
  readonly auth = inject(AuthService);
  private ref = inject(MatDialogRef<WorkLogManageComponent>, { optional: true });

  data = inject(MAT_DIALOG_DATA, { optional: true }) as { driverId?: number } | null;

  loading = signal(false);
  allRows = signal<DriverWorkLogDTO[]>([]);

  searchCtrl = new FormControl<string>('', { nonNullable: true });
  private searchQuery = signal('');

  pageIndex = signal(0);
  pageSize = signal(10);

  readonly driverId = this.data?.driverId
    ?? (this.auth.hasRole('DRIVER') ? this.auth.me()?.account?.userId ?? null : null);
  readonly isDialog = !!this.ref;
  readonly hasDriverScope = this.driverId != null;
  readonly canAdd = computed(() => this.auth.hasRole('ADMIN') || this.auth.hasRole('DRIVER'));
  readonly canManage = computed(() => this.auth.hasRole('ADMIN'));

  cols = this.hasDriverScope
    ? ['startTime', 'endTime', 'breakDuration', 'activityType', 'transportId', 'notes', 'actions']
    : ['driverId', 'startTime', 'endTime', 'breakDuration', 'activityType', 'transportId', 'notes', 'actions'];

  filtered = computed(() => {
    const q = (this.searchQuery() ?? '').trim().toLowerCase();
    const list = this.allRows() ?? [];
    if (!q) return list;

    return list.filter(r => {
      const tid = String(r.transportId ?? '');
      const type = String(r.activityType ?? '').toLowerCase();
      const note = (r.notes ?? '').toLowerCase();
      const id = String(r.id ?? '');
      const did = String(r.driverId ?? '');
      const dname = (r.driverName ?? '').toLowerCase();
      return id.includes(q) || tid.includes(q) || type.includes(q) || note.includes(q) || did.includes(q) || dname.includes(q);
    });
  });

  rows = computed(() => {
    const list = this.filtered();
    const start = this.pageIndex() * this.pageSize();
    const end = start + this.pageSize();
    return list.slice(start, end);
  });

  total = computed(() => this.filtered().length);

  ngOnInit() {
    this.searchQuery.set(this.searchCtrl.value);

    this.searchCtrl.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.searchQuery.set(value ?? '');
        this.pageIndex.set(0);
      });

    this.reload();
  }

  reload() {
    this.loading.set(true);
    const driverId = this.driverId;
    const req$ = this.auth.hasRole('DRIVER')
      ? this.api.listMy()
      : (driverId != null ? this.api.listByDriver(driverId) : this.api.listAll());

    req$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.allRows.set(list ?? []);
          this.pageIndex.set(0);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.notify.error('Failed to load work log');
        }
      });
  }

  onPage(e: PageEvent) {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
  }

  onSort(e: Sort) {
    if (!e.direction) return;

    const dir = e.direction === 'asc' ? 1 : -1;
    const key = e.active as keyof DriverWorkLogDTO;

    const sorted = [...(this.allRows() ?? [])].sort((a, b) => {
      const av = (a[key] ?? '') as any;
      const bv = (b[key] ?? '') as any;
      if (av === bv) return 0;
      return av > bv ? dir : -dir;
    });

    this.allRows.set(sorted);
    this.pageIndex.set(0);
  }

  openCreate() {
    if (!this.canAdd()) {
      this.notify.warn('Only admin or driver can add work log entries');
      return;
    }

    this.dialog.open(WorkLogFormDialogComponent, {
      width: '640px',
      panelClass: 'safe-dialog',
      data: { mode: 'create' as const, driverId: this.driverId ?? null },
    }).afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((ok?: boolean) => ok && this.reload());
  }

  openEdit(item: DriverWorkLogDTO) {
    if (!this.canManage()) {
      this.notify.warn('Only admin can edit work log entries');
      return;
    }
    this.dialog.open(WorkLogFormDialogComponent, {
      width: '640px',
      panelClass: 'safe-dialog',
      data: { mode: 'edit' as const, item },
    }).afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((ok?: boolean) => ok && this.reload());
  }

  delete(item: DriverWorkLogDTO) {
    if (!this.canManage()) {
      this.notify.warn('Only admin can delete work log entries');
      return;
    }
    this.confirm.confirm({
      title: 'Delete work log',
      message: `Delete entry #${item.id}?`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      icon: 'delete_forever',
      confirmColor: 'warn',
    }).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(ok => {
        if (!ok) return;

        this.api.delete(item.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => { this.notify.success('Deleted'); this.reload(); },
            error: () => this.notify.error('Failed to delete')
          });
      });
  }

  close() {
    if (this.ref) this.ref.close();
  }
}



