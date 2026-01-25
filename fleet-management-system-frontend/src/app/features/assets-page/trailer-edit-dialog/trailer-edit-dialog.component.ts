import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs';

import { TrailerService } from '../../../core/services/trailer/trailer.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { TrailerDTO, CreateTrailerRequest } from '../../../core/models/trailer.model';
import { TrailerStatus } from '../../../core/models/trailer-status.model';

@Component({
  selector: 'app-trailer-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  templateUrl: './trailer-edit-dialog.component.html',
  styleUrls: ['./trailer-edit-dialog.component.scss'],
})
export class TrailerEditDialogComponent {
  private ref = inject(MatDialogRef<TrailerEditDialogComponent>);
  private fb = inject(FormBuilder);
  private api = inject(TrailerService);
  private notify = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  data = inject(MAT_DIALOG_DATA) as { trailer: TrailerDTO };
  trailerStatuses = Object.values(TrailerStatus);
  canChangeStatus = !this.data.trailer.inProgressAssigned;

  form = this.fb.group({
    name: [this.data.trailer.name ?? '', [Validators.required]],
    licensePlate: [this.data.trailer.licensePlate ?? '', [Validators.required]],
    trailerStatus: [
      { value: this.data.trailer.trailerStatus ?? TrailerStatus.ACTIVE, disabled: !this.canChangeStatus },
      [Validators.required],
    ],
    payload: [this.data.trailer.payload ?? (null as number | null), [Validators.required, Validators.min(0.01)]],
    volume: [this.data.trailer.volume ?? (null as number | null), [Validators.required, Validators.min(0.01)]],
  });

  close() { this.ref.close(); }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const v = this.form.value;
    const payload = Number(v.payload);
    const volume = Number(v.volume);
    if (!Number.isFinite(payload) || payload <= 0 || !Number.isFinite(volume) || volume <= 0) {
      this.notify.warn('Payload and volume must be positive numbers.');
      return;
    }

    const nextStatus = (this.form.getRawValue().trailerStatus ?? this.data.trailer.trailerStatus) as TrailerStatus;
    const dto: CreateTrailerRequest = {
      name: v.name!,
      licensePlate: v.licensePlate!,
      payload,
      volume,
    };

    const update$ = this.api.updateTrailer(this.data.trailer.id, dto);
    const statusChanged = nextStatus !== this.data.trailer.trailerStatus && this.canChangeStatus;

    (statusChanged
      ? update$.pipe(switchMap(() => this.api.changeStatus(this.data.trailer.id, nextStatus)))
      : update$
    )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.notify.success('Trailer updated'); this.ref.close('ok'); },
        error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to update trailer'),
      });
  }
}



