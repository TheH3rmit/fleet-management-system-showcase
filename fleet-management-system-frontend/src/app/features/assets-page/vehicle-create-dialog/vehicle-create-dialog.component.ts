import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { VehicleService } from '../../../core/services/vehicles/vehicle.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { CreateVehicleRequest } from '../../../core/models/vehicle.model';

@Component({
  selector: 'app-vehicle-create-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './vehicle-create-dialog.component.html',
  styleUrls: ['./vehicle-create-dialog.component.scss'],
})
export class VehicleCreateDialogComponent {
  private ref = inject(MatDialogRef<VehicleCreateDialogComponent>);
  private fb = inject(FormBuilder);
  private api = inject(VehicleService);
  private notify = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  // Dialog data is reserved for future use (e.g. edit).
  data = inject(MAT_DIALOG_DATA, { optional: true }) as any;

  form = this.fb.group({
    manufacturer: ['', [Validators.required]],
    model: ['', [Validators.required]],
    licensePlate: ['', [Validators.required]],
    dateOfProduction: [''],
    mileage: [null as number | null],
    fuelType: [''],
    allowedLoad: [null as number | null],
    insuranceNumber: [''],
  });

  close() { this.ref.close(); }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const v = this.form.value;
    if (v.mileage != null && v.mileage < 0) {
      this.notify.warn('Mileage must be >= 0');
      return;
    }
    if (v.allowedLoad != null && v.allowedLoad < 0) {
      this.notify.warn('Allowed load must be >= 0');
      return;
    }
    const dto: CreateVehicleRequest = {
      manufacturer: v.manufacturer!,
      model: v.model!,
      licensePlate: v.licensePlate!,
      dateOfProduction: v.dateOfProduction?.trim() ? v.dateOfProduction!.trim() : null,
      mileage: v.mileage ?? null,
      fuelType: v.fuelType?.trim() ? v.fuelType!.trim() : null,
      allowedLoad: v.allowedLoad ?? null,
      insuranceNumber: v.insuranceNumber?.trim() ? v.insuranceNumber!.trim() : null,
    };

    this.api.createVehicle(dto)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.notify.success('Vehicle created'); this.ref.close('ok'); },
        error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to create vehicle'),
      });
  }
}



