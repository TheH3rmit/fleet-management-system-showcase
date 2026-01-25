import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { DriverService } from '../../../core/services/driver/driver.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { DriverDTO, UpdateDriverRequest } from '../../../core/models/driver.model';
import { DriverStatus } from '../../../core/models/driver-status.model';
import { of } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-driver-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './driver-edit-dialog.component.html',
  styleUrl: './driver-edit-dialog.component.scss',
})
export class DriverEditDialogComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<DriverEditDialogComponent>);
  private api = inject(DriverService);
  private notify = inject(NotificationService);

  data = inject(MAT_DIALOG_DATA) as { driver: DriverDTO };

  saving = false;
  statuses = Object.values(DriverStatus);

  form = this.fb.group({
    driverLicenseNumber: [''],
    driverLicenseCategory: [''],
    driverLicenseExpiryDate: [''],
    driverStatus: [''],
  });

  ngOnInit(): void {
    const d = this.data.driver;

    this.form.patchValue({
      driverLicenseNumber: d.driverLicenseNumber ?? '',
      driverLicenseCategory: d.driverLicenseCategory ?? '',
      driverLicenseExpiryDate: d.driverLicenseExpiryDate ?? '',
      driverStatus: d.driverStatus ?? '',
    });

    if (this.isStatusLocked()) {
      this.form.get('driverStatus')?.disable({ emitEvent: false });
    }
  }

  submit() {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;

    const v = this.form.getRawValue();

    const current = this.data.driver;
    const driverLicenseNumber = v.driverLicenseNumber?.trim() || null;
    const driverLicenseCategory = v.driverLicenseCategory?.trim() || null;
    const driverLicenseExpiryDate = v.driverLicenseExpiryDate?.trim() || null;
    const statusLocked = this.isStatusLocked();
    const nextStatus = statusLocked
      ? current.driverStatus
      : (v.driverStatus || current.driverStatus) as DriverStatus;

    const dto: UpdateDriverRequest = {
      driverLicenseNumber,
      driverLicenseCategory,
      driverLicenseExpiryDate,
    };

    const detailsChanged =
      driverLicenseNumber !== (current.driverLicenseNumber ?? null) ||
      driverLicenseCategory !== (current.driverLicenseCategory ?? null) ||
      driverLicenseExpiryDate !== (current.driverLicenseExpiryDate ?? null);

    const statusChanged = !statusLocked && nextStatus !== current.driverStatus;

    if (!detailsChanged && !statusChanged) {
      this.ref.close(current);
      return;
    }

    const update$ = detailsChanged
      ? this.api.update(this.data.driver.userId, dto)
      : of(current);

    update$
      .pipe(
        switchMap((updated) =>
          statusChanged ? this.api.changeStatus(updated.userId, nextStatus) : of(updated)
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (updated) => {
          this.notify.success('Driver updated');
          this.ref.close(updated);
        },
        error: (err: any) => {
          this.notify.error(err?.userMessage ?? 'Failed to update driver');
          this.saving = false;
        }
      });
  }

  close() {
    this.ref.close();
  }

  isStatusLocked(): boolean {
    return this.data.driver.driverStatus === DriverStatus.ON_TRANSPORT;
  }
}



