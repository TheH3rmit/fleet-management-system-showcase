import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { StatusHistoryService } from '../../../core/services/status-history/status-history.service';
import { StatusHistoryDTO } from '../../../core/models/status-history.model';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { MatPaginatorModule, PageEvent } from "@angular/material/paginator";
import { MatSortModule, Sort } from "@angular/material/sort";

@Component({
  selector: 'app-status-history-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './status-history-dialog.component.html',
  styleUrls: ['./status-history-dialog.component.scss'],
})
export class StatusHistoryDialogComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private api = inject(StatusHistoryService);
  private notify = inject(NotificationService);
  private ref = inject(MatDialogRef<StatusHistoryDialogComponent>);
  data = inject(MAT_DIALOG_DATA) as { transportId: number };

  loading = signal(false);
  allRows = signal<StatusHistoryDTO[]>([]);

  pageIndex = signal(0);
  pageSize = signal(10);

  cols = ['changedAt', 'status', 'changedBy'];

  rows = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    const end = start + this.pageSize();
    return (this.allRows() ?? []).slice(start, end);
  });

  get transportId(): number { return this.data.transportId; }
  get total(): number { return (this.allRows() ?? []).length; }

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.loading.set(true);
    this.api.getByTransportId(this.transportId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.allRows.set(list ?? []);
          this.pageIndex.set(0);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.notify.error('Failed to load status history');
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
    const key = e.active as keyof StatusHistoryDTO;

    const sorted = [...(this.allRows() ?? [])].sort((a, b) => {
      const av = (a[key] ?? '') as any;
      const bv = (b[key] ?? '') as any;
      if (av === bv) return 0;
      return av > bv ? dir : -dir;
    });

    this.allRows.set(sorted);
    this.pageIndex.set(0);
  }

  close() { this.ref.close(); }
}



