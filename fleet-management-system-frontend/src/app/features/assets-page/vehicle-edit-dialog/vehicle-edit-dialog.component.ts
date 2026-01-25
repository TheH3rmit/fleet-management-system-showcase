import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { switchMap, of } from 'rxjs';

import { VehicleService } from '../../../core/services/vehicles/vehicle.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { VehicleDTO, CreateVehicleRequest } from '../../../core/models/vehicle.model';
import { VehicleStatus } from '../../../core/models/vehicle-status.model';

@Component({
  selector: 'app-vehicle-edit-dialog',
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
  templateUrl: './vehicle-edit-dialog.component.html',
  styleUrls: ['./vehicle-edit-dialog.component.scss'],
})
export class VehicleEditDialogComponent {
  private ref = inject(MatDialogRef<VehicleEditDialogComponent>);
  private fb = inject(FormBuilder);
  private api = inject(VehicleService);
  private notify = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  data = inject(MAT_DIALOG_DATA) as { vehicle: VehicleDTO };
  vehicleStatuses = Object.values(VehicleStatus);
  canChangeStatus = !this.data.vehicle.inProgressAssigned;

  form = this.fb.group({
    manufacturer: [this.data.vehicle.manufacturer ?? '', [Validators.required]],
    model: [this.data.vehicle.model ?? '', [Validators.required]],
    licensePlate: [this.data.vehicle.licensePlate ?? '', [Validators.required]],
    vehicleStatus: [
      { value: this.data.vehicle.vehicleStatus ?? VehicleStatus.ACTIVE, disabled: !this.canChangeStatus },
      [Validators.required],
    ],
    dateOfProduction: [this.data.vehicle.dateOfProduction ?? ''],
    mileage: [this.data.vehicle.mileage ?? (null as number | null)],
    fuelType: [this.data.vehicle.fuelType ?? ''],
    allowedLoad: [this.data.vehicle.allowedLoad ?? (null as number | null)],
    insuranceNumber: [this.data.vehicle.insuranceNumber ?? ''],
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
    const nextStatus = (this.form.getRawValue().vehicleStatus ?? this.data.vehicle.vehicleStatus) as VehicleStatus;
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

    const update$ = this.api.updateVehicle(this.data.vehicle.id, dto);
    const statusChanged = nextStatus !== this.data.vehicle.vehicleStatus && this.canChangeStatus;

    (statusChanged
      ? update$.pipe(switchMap(() => this.api.changeStatus(this.data.vehicle.id, nextStatus)))
      : update$
    )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.notify.success('Vehicle updated'); this.ref.close('ok'); },
        error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to update vehicle'),
      });
  }
}



