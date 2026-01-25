import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { CargoService } from '../../../core/services/cargo/cargo.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { CargoDTO } from '../../../core/models/cargo.model';

@Component({
  selector: 'app-cargo-edit-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './cargo-edit-dialog.component.html',
  styleUrls: ['./cargo-edit-dialog.component.scss'],
})
export class CargoEditDialogComponent {
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<CargoEditDialogComponent>);
  private api = inject(CargoService);
  private notify = inject(NotificationService);

  data = inject(MAT_DIALOG_DATA) as
    | { mode: 'create' }
    | { mode: 'edit'; cargo: CargoDTO };

  saving = false;

  form = this.fb.group({
    transportId: [null as number | null, [Validators.required]], // create only
    cargoDescription: ['', [Validators.required, Validators.maxLength(2000)]],
    weightKg: [null as number | null, [Validators.required, Validators.min(0.01)]],
    volumeM3: [null as number | null, [Validators.required, Validators.min(0.01)]],
    pickupDate: [''],
    deliveryDate: [''],
  });

  constructor() {
    if (this.data.mode === 'edit') {
      const c = this.data.cargo;
      this.form.patchValue({
        transportId: c.transportId ?? null,
        cargoDescription: c.cargoDescription ?? '',
        weightKg: c.weightKg ?? null,
        volumeM3: c.volumeM3 ?? null,
        pickupDate: c.pickupDate ? this.toLocal(c.pickupDate) : '',
        deliveryDate: c.deliveryDate ? this.toLocal(c.deliveryDate) : '',
      });

      // Business rule: transportId is immutable in edit mode.
      this.form.controls.transportId.disable();
    }
  }

  private toIsoOrNull(v: string | null | undefined): string | null {
    const s = (v ?? '').trim();
    if (!s) return null;
    return new Date(s).toISOString();
  }

  private toLocal(iso: string): string {
    const d = new Date(iso);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  submit() {
    if (this.form.invalid || this.saving) return;

    this.saving = true;

    const v = this.form.getRawValue(); // Use raw values because transportId can be disabled.
    // Client-side validation mirrors backend cargo rules.
    const weightKg = Number(v.weightKg);
    if (!Number.isFinite(weightKg) || weightKg <= 0) {
      this.saving = false;
      this.notify.warn('Provide a valid cargo weight');
      return;
    }

    const volumeM3 = Number(v.volumeM3);
    if (!Number.isFinite(volumeM3) || volumeM3 <= 0) {
      this.saving = false;
      this.notify.warn('Provide a valid cargo volume');
      return;
    }

    const pickupDate = this.toIsoOrNull(v.pickupDate);
    const deliveryDate = this.toIsoOrNull(v.deliveryDate);
    if (pickupDate && deliveryDate && new Date(deliveryDate).getTime() < new Date(pickupDate).getTime()) {
      this.saving = false;
      this.notify.warn('Delivery date must be after pickup date');
      return;
    }

    const payload = {
      cargoDescription: (v.cargoDescription ?? '').trim(),
      weightKg,
      volumeM3,
      pickupDate,
      deliveryDate,
    };

    if (this.data.mode === 'create') {
      this.api.create({
        ...payload,
        transportId: v.transportId!,
      }).pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notify.success('Cargo created');
            this.ref.close(true);
          },
          error: (err) => {
            this.saving = false;
            this.notify.error(err?.userMessage ?? err?.error?.message ?? 'Failed to create cargo');
          }
        });
      return;
    }

    // edit
    this.api.update(this.data.cargo.id, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notify.success('Cargo updated');
          this.ref.close(true);
        },
        error: (err) => {
          this.saving = false;
          this.notify.error(err?.userMessage ?? err?.error?.message ?? 'Failed to update cargo');
        }
      });
  }

  close() {
    this.ref.close(false);
  }
}



