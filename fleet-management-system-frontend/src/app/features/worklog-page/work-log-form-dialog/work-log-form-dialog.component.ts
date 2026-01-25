import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { DriverWorkLogService } from '../../../core/services/driver-work-log/driver-work-log.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { ActivityType, ACTIVITY_TYPE_OPTIONS } from '../../../core/models/activity-type.model';
import { CreateDriverWorkLogRequest, DriverWorkLogDTO } from '../../../core/models/driver-work-log.model';
import { TransportService } from '../../../core/services/transport/transport.service';
import { TransportDTO } from '../../../core/models/transport.model';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DriverService } from '../../../core/services/driver/driver.service';
import { DriverDTO } from '../../../core/models/driver.model';
import { AuthService } from '../../../core/services/auth/auth.service';

@Component({
  selector: 'app-work-log-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './work-log-form-dialog.component.html',
  styleUrls: ['./work-log-form-dialog.component.scss'],
})
export class WorkLogFormDialogComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<WorkLogFormDialogComponent>);
  private api = inject(DriverWorkLogService);
  private notify = inject(NotificationService);
  private transportApi = inject(TransportService);
  private driverApi = inject(DriverService);
  private auth = inject(AuthService);
  transports = signal<TransportDTO[]>([]);
  loadingTransports = signal(false);
  drivers = signal<DriverDTO[]>([]);
  loadingDrivers = signal(false);

  data = inject(MAT_DIALOG_DATA) as
    | { mode: 'create'; driverId?: number | null }
    | { mode: 'edit'; item: DriverWorkLogDTO };

  saving = signal(false);
  types = signal<ActivityType[]>(ACTIVITY_TYPE_OPTIONS);

  form = this.fb.group({
    driverId: [null as number | null, [Validators.required]],
    transportId: [null as number | null, [Validators.required]],     // wymagane przez backend
    activityType: [null as ActivityType | null, [Validators.required]],
    startTime: ['', [Validators.required]],                          // datetime-local
    endTime: [''],                                                   // datetime-local
    breakDuration: [null as number | null],
    notes: [''],
  });

  ngOnInit() {
    const currentDriverId = this.auth.me()?.account?.userId ?? null;
    const driverId =
      this.data.mode === 'create'
        ? (this.data.driverId ?? currentDriverId)
        : this.data.item.driverId;

    if (this.data.mode === 'create') {
      // Business rule: drivers can create logs only for themselves.
      if (this.auth.hasRole('DRIVER') && currentDriverId != null) {
        this.form.patchValue({ driverId: currentDriverId });
        this.form.controls.driverId.disable();
        this.loadTransports(currentDriverId);
        return;
      }

      if (driverId != null) {
        this.form.patchValue({ driverId });
        this.form.controls.driverId.disable();
        this.loadTransports(driverId);
      } else {
        this.loadDrivers();
      }

      this.form.controls.driverId.valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((id) => {
          if (id == null) {
            this.transports.set([]);
            return;
          }
          this.loadTransports(id);
        });

      return;
    }

    const it = this.data.item;

    this.form.patchValue({
      driverId: it.driverId,
      transportId: it.transportId,
      activityType: it.activityType,
      startTime: it.startTime ? this.toLocal(it.startTime) : '',
      endTime: it.endTime ? this.toLocal(it.endTime) : '',
      breakDuration: it.breakDuration ?? null,
      notes: it.notes ?? '',
    });

    // Business rule: keep driver/transport links immutable in edit mode.
    this.form.controls.driverId.disable();
    this.form.controls.transportId.disable();
  }

  submit() {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);

    const raw = this.form.getRawValue();
    const startIso = this.toIso(raw.startTime);
    const endIso = this.toIso(raw.endTime);
    if (!startIso) {
      this.saving.set(false);
      this.notify.warn('Start time is required');
      return;
    }
    if (endIso && new Date(endIso).getTime() < new Date(startIso).getTime()) {
      this.saving.set(false);
      this.notify.warn('End time must be after start time');
      return;
    }
    if (raw.breakDuration != null && raw.breakDuration < 0) {
      this.saving.set(false);
      this.notify.warn('Break duration must be >= 0');
      return;
    }

    const payload: CreateDriverWorkLogRequest = {
      startTime: startIso,
      endTime: endIso,
      breakDuration: raw.breakDuration ?? null,
      notes: (raw.notes ?? '').trim() || null,
      driverId: raw.driverId!,
      transportId: raw.transportId!,
      activityType: raw.activityType!,
    };

    if (this.data.mode === 'create') {
      const req$ = this.auth.hasRole('DRIVER')
        ? this.api.createMy(payload)
        : this.api.create(payload);

      req$
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => { this.notify.success('Created'); this.ref.close(true); },
          error: (err) => { this.saving.set(false); this.notify.error(err?.error?.message ?? 'Create failed'); }
        });
      return;
    }

    this.api.update(this.data.item.id, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.notify.success('Saved'); this.ref.close(true); },
        error: (err) => { this.saving.set(false); this.notify.error(err?.error?.message ?? 'Save failed'); }
      });
  }

  close() { this.ref.close(false); }

  // datetime-local -> ISO
  private toIso(v: string | null | undefined): string | null {
    const s = (v ?? '').trim();
    if (!s) return null;
    return new Date(s).toISOString();
  }

  // ISO -> yyyy-MM-ddTHH:mm
  private toLocal(iso: string): string {
    const d = new Date(iso);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  private loadDrivers() {
    this.loadingDrivers.set(true);

    this.driverApi.getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.drivers.set(list ?? []);
          this.loadingDrivers.set(false);
        },
        error: () => {
          this.loadingDrivers.set(false);
          this.notify.error('Failed to load drivers');
        }
      });
  }

  private loadTransports(driverId: number) {
    this.loadingTransports.set(true);

    this.transportApi.getByDriver(driverId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          this.transports.set(list ?? []);
          this.loadingTransports.set(false);
        },
        error: () => {
          this.loadingTransports.set(false);
          this.notify.error('Failed to load driver transports');
        }
      });
  }

  get editedItem(): DriverWorkLogDTO | null {
    return this.data.mode === 'edit' ? this.data.item : null;
  }
}



