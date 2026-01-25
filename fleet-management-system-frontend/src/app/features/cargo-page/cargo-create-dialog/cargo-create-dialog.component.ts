import { Component, DestroyRef, inject, OnInit } from '@angular/core';
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
  selector: 'app-cargo-create-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './cargo-create-dialog.component.html',
  styleUrl: './cargo-create-dialog.component.scss',
})
export class CargoCreateDialogComponent implements OnInit{
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<CargoCreateDialogComponent>);
  private api = inject(CargoService);
  private notify = inject(NotificationService);


  data = inject(MAT_DIALOG_DATA) as { transportId: number | null; draft?: any };

  creating = false;

  form = this.fb.group({
    cargoDescription: ['', [Validators.required, Validators.maxLength(2000)]],
    weightKg: [null as number | null, [Validators.required, Validators.min(0.01)]],
    volumeM3: [null as number | null, [Validators.required, Validators.min(0.01)]],
    pickupDate: [''],   // datetime-local
    deliveryDate: [''], // datetime-local
  });


  ngOnInit(): void {
    if (this.data?.draft) {
      this.form.patchValue({
        cargoDescription: this.data.draft.cargoDescription ?? '',
        weightKg: this.data.draft.weightKg ?? null,
        volumeM3: this.data.draft.volumeM3 ?? null,
        pickupDate: this.data.draft.pickupDate ? this.toLocal(this.data.draft.pickupDate) : '',
        deliveryDate: this.data.draft.deliveryDate ? this.toLocal(this.data.draft.deliveryDate) : '',
      });
    }
  }

  private toIsoOrNull(v: string | null | undefined): string | null {
    const s = (v ?? '').trim();
    if (!s) return null;
    // datetime-local => "2026-01-01T10:30"
    return new Date(s).toISOString();
  }

  submit() {
    if (this.form.invalid || this.creating) return;

    const v = this.form.value;
    // Client-side validation mirrors backend cargo rules.
    const weightKg = Number(v.weightKg);
    if (!Number.isFinite(weightKg) || weightKg <= 0) {
      this.notify.warn('Provide a valid cargo weight');
      return;
    }

    const volumeM3 = Number(v.volumeM3);
    if (!Number.isFinite(volumeM3) || volumeM3 <= 0) {
      this.notify.warn('Provide a valid cargo volume');
      return;
    }

    const pickupDate = this.toIsoOrNull(v.pickupDate);
    const deliveryDate = this.toIsoOrNull(v.deliveryDate);
    if (pickupDate && deliveryDate && new Date(deliveryDate).getTime() < new Date(pickupDate).getTime()) {
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

    // Draft mode: transport does not exist yet.
    if (this.data.transportId == null) {
      this.ref.close(payload);   // zwracamy draft
      return;
    }

    // Normal mode: attach cargo to an existing transport.
    this.creating = true;

    this.api.createForTransport(this.data.transportId, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (created: CargoDTO) => {
          this.notify.success('Cargo created');
          this.ref.close(created);
        },
        error: (err) => {
          this.creating = false;
          this.notify.error(err?.userMessage ?? err?.error?.message ?? 'Failed to create cargo');
        }
      });
  }

  close() {
    this.ref.close();
  }

  private toLocal(iso: string): string {
    // ISO -> yyyy-MM-ddTHH:mm dla datetime-local
    const d = new Date(iso);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
}



