import { Component, DestroyRef, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { forkJoin } from 'rxjs';

import { DriverService } from '../../../core/services/driver/driver.service';
import { CargoDTO } from '../../../core/models/cargo.model';
import { TransportDTO } from '../../../core/models/transport.model';
import { TransportStatus } from '../../../core/models/transport-status.model';

@Component({
  selector: 'app-my-cargo',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
  ],
  templateUrl: './my-cargo.component.html',
  styleUrl: './my-cargo.component.scss'
})
export class MyCargoComponent {
  private api = inject(DriverService);
  private destroyRef = inject(DestroyRef);
  loading = signal(false);
  error = signal<string | null>(null);
  cargo = signal<CargoDTO[]>([]);
  transports = signal<TransportDTO[]>([]);
  viewMode = signal<'current' | 'all'>('current');

  currentTransportId = computed(() => {
    const list = this.transports() ?? [];
    const current = list.find(t => t.status === TransportStatus.IN_PROGRESS)
      ?? list.find(t => t.status === TransportStatus.ACCEPTED);
    return current?.id ?? null;
  });

  filteredCargo = computed(() => {
    const list = this.cargo() ?? [];
    if (this.viewMode() === 'all') return list;
    const currentId = this.currentTransportId();
    if (!currentId) return [];
    return list.filter(c => c.transportId === currentId);
  });

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      cargo: this.api.getMyCargo(),
      transports: this.api.getMyTransports()
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ cargo, transports }) => {
          this.cargo.set(cargo ?? []);
          this.transports.set(transports ?? []);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.error.set('Failed to load cargo.');
        },
      });
  }

  setMode(mode: 'current' | 'all') {
    this.viewMode.set(mode);
  }
}



