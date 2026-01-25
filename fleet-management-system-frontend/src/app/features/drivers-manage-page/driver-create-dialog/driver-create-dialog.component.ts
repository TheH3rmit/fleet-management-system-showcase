import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { DriverService } from '../../../core/services/driver/driver.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { CreateDriverRequest, DriverDTO } from '../../../core/models/driver.model';
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { UserResponseDTO } from "../../../core/models/user.model";
import { UserService } from '../../../core/services/user/user.service';
import { debounceTime, switchMap, distinctUntilChanged, of, Observable, finalize, catchError } from "rxjs";
import { MatIconModule } from "@angular/material/icon";
import { Page } from '../../../core/models/page.model';
import { map } from "rxjs/operators";

@Component({
  selector: 'app-driver-create-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatAutocompleteModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  templateUrl: './driver-create-dialog.component.html',
  styleUrl: './driver-create-dialog.component.scss',
})
export class DriverCreateDialogComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<DriverCreateDialogComponent>);
  private api = inject(DriverService);
  private notify = inject(NotificationService);
  private usersApi = inject(UserService);

  data = inject(MAT_DIALOG_DATA, { optional: true }) as { userId?: number } | null;

  creating = false;

  loadingUsers = false;
  userQueryCtrl = new FormControl<string | UserResponseDTO>('', { nonNullable: true });
  userOptions = signal<UserResponseDTO[]>([]);
  selectedUser = signal<UserResponseDTO | null>(null);

  form = this.fb.group({
    userId: [this.data?.userId ?? (null as number | null), [Validators.required]],
    driverLicenseNumber: [null as string | null],
    driverLicenseCategory: [''],
    driverLicenseExpiryDate: [''], // YYYY-MM-DD
  });

  ngOnInit() {
    this.userQueryCtrl.valueChanges
      .pipe(
        debounceTime(250),

        // normalize input to string for search
        map((v) => (typeof v === 'string' ? v : this.displayUser(v))),

        distinctUntilChanged(),

        switchMap((raw): Observable<UserResponseDTO[]> => {
          const query = (raw ?? '').trim();

          // Avoid server calls for empty/short input.
          if (!query || query.length < 2) {
            this.loadingUsers = false;
            this.userOptions.set([]);
            return new Observable<UserResponseDTO[]>(sub => { sub.next([]); sub.complete(); });
          }

          this.loadingUsers = true;

          return this.usersApi.list({ q: query, page: 0, size: 10 }).pipe(
            map((p: Page<UserResponseDTO>) => p.content ?? []),

            catchError((err) => {
              this.userOptions.set([]);

              // Abort/cancel -> do not spam errors.
              if (err?.status === 0) {
                return new Observable<UserResponseDTO[]>(sub => { sub.next([]); sub.complete(); });
              }

              const status = err?.status;
              const msg =
                status === 403 ? 'No permission to search users (403)' :
                  status === 404 ? 'Users endpoint not found (404)' :
                    err?.error?.message ?? 'Failed to search users';

              this.notify.error(msg);
              return new Observable<UserResponseDTO[]>(sub => { sub.next([]); sub.complete(); });
            }),

            finalize(() => (this.loadingUsers = false))
          );
        })
      )
      .subscribe((list) => this.userOptions.set(list ?? []));
  }

  canSubmit = computed(() => {
    const userOk = !!this.selectedUser() && !!this.form.value.userId;
    return userOk && this.form.valid && !this.creating;
  });


  displayUser = (u: UserResponseDTO | string | null): string => {
    if (!u) return '';
    if (typeof u === 'string') return u;

    const parts = [u.firstName, u.middleName, u.lastName].filter(Boolean);
    const name = parts.join(' ').trim();
    const email = (u.email ?? '').trim();

    if (name && email) return `${name} - ${email}`;
    return name || email;
  };

  onUserSelected(u: UserResponseDTO) {
    this.selectedUser.set(u);
    this.form.patchValue({ userId: u.id });

    // Avoid triggering another search when setting display text.
    this.userQueryCtrl.setValue(this.displayUser(u), { emitEvent: false });
    this.userOptions.set([]);
  }

  clearUser() {
    this.selectedUser.set(null);
    this.form.patchValue({ userId: null });
    this.userQueryCtrl.setValue('', { emitEvent: true });
  }

  submit() {
    if (!this.selectedUser() || !this.form.value.userId) {
      this.notify.warn('Select user first');
      return;
    }

    if (this.form.invalid || this.creating) {
      this.form.markAllAsTouched();
      return;
    }

    this.creating = true;

    const v = this.form.value;

    const dto: CreateDriverRequest = {
      userId: v.userId!,
      driverLicenseNumber: v.driverLicenseNumber?.toString().trim()
        ? v.driverLicenseNumber!.toString().trim()
        : null,
      driverLicenseCategory: v.driverLicenseCategory?.trim()
        ? v.driverLicenseCategory.trim()
        : null,
      driverLicenseExpiryDate: v.driverLicenseExpiryDate?.trim()
        ? v.driverLicenseExpiryDate.trim()
        : null,
    };

    this.api.create(dto)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (created: DriverDTO) => {
          this.notify.success('Driver created');
          this.ref.close(created);
        },
        error: (err: any) => {
          this.notify.error(err?.userMessage ?? err?.error?.error ?? 'Failed to create driver');
          this.creating = false;
        }
      });
  }

  close() {
    this.ref.close();
  }
}




