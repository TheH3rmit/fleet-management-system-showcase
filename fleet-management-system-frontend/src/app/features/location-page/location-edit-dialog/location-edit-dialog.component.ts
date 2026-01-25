import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { LocationService } from '../../../core/services/location/location.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { CreateLocationRequest, LocationDTO } from '../../../core/models/location.model';

@Component({
  selector: 'app-location-edit-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './location-edit-dialog.component.html',
  styleUrl: './location-edit-dialog.component.scss',
})
export class LocationEditDialogComponent {
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<LocationEditDialogComponent>);
  private api = inject(LocationService);
  private notify = inject(NotificationService);

  data = inject(MAT_DIALOG_DATA) as { location: LocationDTO };

  saving = false;

  form = this.fb.group({
    city: [this.data.location.city ?? '', [Validators.required]],
    street: [this.data.location.street ?? '', [Validators.required]],
    buildingNumber: [this.data.location.buildingNumber ?? '', [Validators.required]],
    postcode: [this.data.location.postcode ?? '', [Validators.required]],
    country: [this.data.location.country ?? 'PL'],
    latitude: [this.data.location.latitude ?? null],
    longitude: [this.data.location.longitude ?? null],
  });

  submit() {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;

    const v = this.form.value;
    if (!this.validateCoordinates(v.latitude, v.longitude)) {
      this.saving = false;
      return;
    }
    const dto: CreateLocationRequest = {
      city: v.city!,
      street: v.street!,
      buildingNumber: v.buildingNumber!,
      postcode: v.postcode!,
      country: v.country ?? null,
      latitude: v.latitude ?? null,
      longitude: v.longitude ?? null,
    };

    this.api.update(this.data.location.id, dto)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated: LocationDTO) => this.ref.close(updated),
        error: (err: any) => {
          this.notify.error(err?.userMessage ?? 'Failed to update location');
          this.saving = false;
        }
      });
  }

  close() { this.ref.close(null); }

  private validateCoordinates(latitude: number | null | undefined, longitude: number | null | undefined): boolean {
    if (latitude == null && longitude == null) return true;
    if (latitude == null || longitude == null) {
      this.notify.warn('Provide both latitude and longitude or leave both empty');
      return false;
    }
    if (latitude < -90 || latitude > 90) {
      this.notify.warn('Latitude must be between -90 and 90');
      return false;
    }
    if (longitude < -180 || longitude > 180) {
      this.notify.warn('Longitude must be between -180 and 180');
      return false;
    }
    return true;
  }
}



