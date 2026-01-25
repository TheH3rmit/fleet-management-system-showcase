import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LocationService } from '../../../core/services/location/location.service';
import { CreateLocationRequest, LocationDTO } from '../../../core/models/location.model';
import { NotificationService } from '../../../core/services/notification/notification.service';

@Component({
  selector: 'app-location-create-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './location-create-dialog.component.html',
  styleUrl: './location-create-dialog.component.scss',
})
export class LocationCreateDialogComponent {
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<LocationCreateDialogComponent>);
  private api = inject(LocationService);
  private notify = inject(NotificationService);

  saving = false;

  form = this.fb.group({
    street: ['', [Validators.required]],
    buildingNumber: ['', [Validators.required]],
    city: ['', [Validators.required]],
    postcode: ['', [Validators.required]],
    country: ['Poland', [Validators.required]],
    latitude: [null as number | null],
    longitude: [null as number | null],
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
      street: v.street!,
      buildingNumber: v.buildingNumber!,
      city: v.city!,
      postcode: v.postcode!,
      country: v.country!,
      latitude: v.latitude ?? null,
      longitude: v.longitude ?? null,
    };

    this.api.create(dto)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (created: LocationDTO) => this.ref.close(created),
        error: (err: any) => {
          this.notify.error(err?.userMessage ?? 'Failed to create location');
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



