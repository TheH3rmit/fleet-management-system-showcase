import { Component, DestroyRef, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';

import { DriverService } from '../../../core/services/driver/driver.service';
import { StatusHistoryDTO } from '../../../core/models/status-history.model';
import { TransportDTO } from '../../../core/models/transport.model';
import { TransportStatus } from '../../../core/models/transport-status.model';

@Component({
  selector: 'app-my-timeline',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
  ],
  templateUrl: './my-timeline.component.html',
  styleUrl: './my-timeline.component.scss'
})
export class MyTimelineComponent {
  private api = inject(DriverService);
  private destroyRef = inject(DestroyRef);

  loading = signal(false);
  error = signal<string | null>(null);
  timeline = signal<StatusHistoryDTO[]>([]);
  transports = signal<TransportDTO[]>([]);

  viewMode = signal<'current' | 'history'>('current');
  selectedTransportId = signal<number | null>(null);

  cols = ['changedAt', 'entity', 'status', 'note'];

  currentTransportId = computed(() => {
    const list = this.transports() ?? [];
    const current = list.find(t => t.status === TransportStatus.IN_PROGRESS)
      ?? list.find(t => t.status === TransportStatus.ACCEPTED);
    return current?.id ?? null;
  });

  transportOptions = computed(() => {
    const list = this.transports() ?? [];
    if (this.viewMode() === 'current') {
      const currentId = this.currentTransportId();
      return currentId ? list.filter(t => t.id === currentId) : [];
    }
    return list;
  });

  filteredTimeline = computed(() => {
    const id = this.selectedTransportId();
    if (!id) return [];
    return (this.timeline() ?? []).filter(x => x.transportId === id);
  });

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      timeline: this.api.getMyTimeline(),
      transports: this.api.getMyTransports()
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ timeline, transports }) => {
          this.timeline.set(timeline ?? []);
          this.transports.set(transports ?? []);
          this.applyDefaultSelection();
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.error.set('Failed to load timeline.');
        },
      });
  }

  setMode(mode: 'current' | 'history') {
    this.viewMode.set(mode);
    this.applyDefaultSelection();
  }

  onTransportChange(id: number) {
    this.selectedTransportId.set(id ?? null);
  }

  private applyDefaultSelection() {
    if (this.viewMode() === 'current') {
      this.selectedTransportId.set(this.currentTransportId());
      return;
    }

    if (!this.selectedTransportId() && (this.transports()?.length ?? 0) > 0) {
      this.selectedTransportId.set(this.transports()[0].id);
    }
  }

  // defensywne mapowanie (zeby nie wywalilo jak DTO ma inne nazwy pol)
  changedAt(x: any): string | null {
    return x?.changedAt ?? x?.createdAt ?? x?.timestamp ?? null;
  }

  entity(x: any): string {
    if (x?.transportId != null) return `Transport #${x.transportId}`;
    if (x?.cargoId != null) return `Cargo #${x.cargoId}`;
    if (x?.entityType) return `${x.entityType}${x.entityId != null ? ' #' + x.entityId : ''}`;
    return 'N/A';
  }

  status(x: any): string {
    return x?.status ?? x?.newStatus ?? x?.toStatus ?? 'N/A';
  }

  note(x: any): string {
    return x?.note ?? x?.reason ?? x?.description ?? 'N/A';
  }
}



