import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { TrailerService } from '../../../core/services/trailer/trailer.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { CreateTrailerRequest } from '../../../core/models/trailer.model';

@Component({
  selector: 'app-trailer-create-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './trailer-create-dialog.component.html',
  styleUrls: ['./trailer-create-dialog.component.scss'],
})
export class TrailerCreateDialogComponent {
  private ref = inject(MatDialogRef<TrailerCreateDialogComponent>);
  private fb = inject(FormBuilder);
  private api = inject(TrailerService);
  private notify = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  data = inject(MAT_DIALOG_DATA, { optional: true }) as any;

  form = this.fb.group({
    name: ['', [Validators.required]],
    licensePlate: ['', [Validators.required]],
    payload: [null as number | null, [Validators.required, Validators.min(0.01)]],
    volume: [null as number | null, [Validators.required, Validators.min(0.01)]],
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

    const dto: CreateTrailerRequest = {
      name: v.name!.trim(),
      licensePlate: v.licensePlate!.trim(),
      payload,
      volume,
    };

    this.api.createTrailer(dto)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.notify.success('Trailer created'); this.ref.close('ok'); },
        error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to create trailer'),
      });
  }
}



