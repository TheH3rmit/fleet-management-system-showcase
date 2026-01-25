import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';

import { DriverService } from '../../../core/services/driver/driver.service';
import { TransportDTO } from '../../../core/models/transport.model';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { TransportService } from '../../../core/services/transport/transport.service';
import { TransportStatus } from '../../../core/models/transport-status.model';

@Component({
  selector: 'app-driver-transports',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTooltipModule,
  ],
  templateUrl: './driver-transports.component.html',
  styleUrl: './driver-transports.component.scss'
})
export class DriverTransportsComponent {
  private driverApi = inject(DriverService);
  private transportApi = inject(TransportService);
  private notify = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  loading = signal(false);
  error = signal<string | null>(null);
  transports = signal<TransportDTO[]>([]);

  // request locks per row (anti-spam)
  private busyIds = signal<Set<number>>(new Set());

  cols = ['id', 'status', 'vehicleId', 'trailerId', 'plannedStartAt', 'actions'];

  hasAny = computed(() => (this.transports()?.length ?? 0) > 0);
  hasInProgress = computed(() =>
    (this.transports() ?? []).some(t => t.status === TransportStatus.IN_PROGRESS)
  );
  currentTransport = computed(() =>
    (this.transports() ?? []).find(t => t.status === TransportStatus.IN_PROGRESS)
    ?? (this.transports() ?? []).find(t => t.status === TransportStatus.ACCEPTED)
    ?? null
  );

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set(null);

    this.driverApi.getMyTransports()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.transports.set(list ?? []);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.error.set('Failed to load transports.');
        },
      });
  }

  // --- helpers ---
  isBusy(t: TransportDTO): boolean {
    return this.busyIds().has(t.id);
  }

  private setBusy(id: number, v: boolean) {
    const next = new Set(this.busyIds());
    if (v) next.add(id);
    else next.delete(id);
    this.busyIds.set(next);
  }

  private replaceRow(updated: TransportDTO) {
    const next = this.transports().map((x) => (x.id === updated.id ? updated : x));
    this.transports.set(next);
  }

  vehicleLabel(t: TransportDTO) {
    return t.vehicleLabel ?? (t.vehicleId != null ? `#${t.vehicleId}` : 'N/A');
  }

  trailerLabel(t: TransportDTO) {
    return t.trailerLabel ?? (t.trailerId != null ? `#${t.trailerId}` : 'N/A');
  }

  // --- available status logic  ---
  canAccept(t: TransportDTO): boolean {
    return t.status === TransportStatus.PLANNED && !this.hasInProgress();
  }

  canStart(t: TransportDTO): boolean {
    return t.status === TransportStatus.ACCEPTED && !this.hasInProgress();
  }

  canFinish(t: TransportDTO): boolean {
    return t.status === TransportStatus.IN_PROGRESS;
  }

  actionTooltip(t: TransportDTO, action: 'accept' | 'start' | 'finish'): string {
    if (this.isBusy(t)) return 'Action in progress';

    if (action === 'accept') {
      if (t.status !== TransportStatus.PLANNED) return 'Cannot accept: only PLANNED transports can be accepted.';
      if (this.hasInProgress()) return 'Finish current transport first.';
      return 'Accept transport';
    }

    if (action === 'start') {
      if (t.status !== TransportStatus.ACCEPTED) return 'Cannot start: only ACCEPTED transports can be started.';
      if (this.hasInProgress()) return 'Finish current transport first.';
      return 'Start transport';
    }

    if (t.status !== TransportStatus.IN_PROGRESS) return 'Cannot finish: only IN_PROGRESS transports can be finished.';
    return 'Finish transport';
  }

  // --- driver actions ---
  onAccept(t: TransportDTO) {
    if (!this.canAccept(t) || this.isBusy(t)) return;

    this.setBusy(t.id, true);
    this.transportApi
      .accept(t.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.replaceRow(updated);
          this.notify.success('Transport accepted');
          this.setBusy(t.id, false);
        },
        error: (err: any) => {
          this.notify.error(err?.userMessage ?? 'Failed to accept transport.');
          this.setBusy(t.id, false);
        },
      });
  }

  onStart(t: TransportDTO) {
    if (!this.canStart(t) || this.isBusy(t)) return;

    this.setBusy(t.id, true);
    this.transportApi
      .start(t.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.replaceRow(updated);
          this.notify.success('Transport started');
          this.setBusy(t.id, false);
        },
        error: (err: any) => {
          this.notify.error(err?.userMessage ?? 'Failed to start transport.');
          this.setBusy(t.id, false);
        },
      });
  }

  onFinish(t: TransportDTO) {
    if (!this.canFinish(t) || this.isBusy(t)) return;

    this.setBusy(t.id, true);
    this.transportApi
      .finish(t.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.replaceRow(updated);
          this.notify.success('Transport finished');
          this.setBusy(t.id, false);
        },
        error: (err: any) => {
          this.notify.error(err?.userMessage ?? 'Failed to finish transport.');
          this.setBusy(t.id, false);
        },
      });
  }
}



