import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

import { NotificationService } from '../../../core/services/notification/notification.service';
import { UserService } from '../../../core/services/user/user.service';
import { AccountService } from '../../../core/services/account/account.service';

import { UserResponseDTO, UserCreateDTO, UserUpdateDTO } from '../../../core/models/user.model';
import { AccountResponseDTO } from '../../../core/models/account.model';
import { UserRole } from '../../../core/models/user-role.model';
import { AccountStatus } from '../../../core/models/account-status.model';
import { AdminService } from '../../../core/services/admin/admin.service';
import { AdminCreateUserWithAccountRequest, AdminCreateUserWithAccountResponseDTO } from "../../../core/models/admin.model";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

@Component({
  selector: 'app-admin-user-with-account-dialog',
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
  templateUrl: './admin-user-account-dialog.component.html',
  styleUrl: './admin-user-account-dialog.component.scss'
})
export class AdminUserAccountDialogComponent {
  private ref = inject(MatDialogRef<AdminUserAccountDialogComponent>);
  private fb = inject(FormBuilder);
  private notify = inject(NotificationService);
  private usersApi = inject(UserService);
  private accountsApi = inject(AccountService);
  private adminApi = inject(AdminService);
  private destroyRef = inject(DestroyRef);


  roles = Object.values(UserRole);
  statuses = Object.values(AccountStatus);

  data = inject(MAT_DIALOG_DATA) as {
    mode: 'create' | 'edit';
    title: string;
    user?: UserResponseDTO;
    account?: AccountResponseDTO | null;
  };

  isEdit = this.data.mode === 'edit';


  form = this.fb.group({
    // USER
    firstName: [this.data.user?.firstName ?? '', [Validators.required]],
    middleName: [this.data.user?.middleName ?? ''],
    lastName: [this.data.user?.lastName ?? '', [Validators.required]],
    email: [this.data.user?.email ?? '', [Validators.required, Validators.email]],
    phone: [this.data.user?.phone ?? ''],
    birthDate: [this.data.user?.birthDate?.substring(0, 10) ?? ''],

    // ACCOUNT
    login: [
      { value: this.data.account?.login ?? '', disabled: this.isEdit },
      this.isEdit ? [] : [Validators.required, Validators.minLength(3)],
    ],
    password: ['' , this.isEdit ? [] : [Validators.required, Validators.minLength(6)]],

    status: [this.data.account?.status ?? AccountStatus.ACTIVE, [Validators.required]],
    roles: [this.data.account?.roles ?? [UserRole.DRIVER], [Validators.required]],

    // EDIT only: optional admin reset password
    resetPassword: [''],
  });

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.value;
    if (!v.roles || v.roles.length === 0) {
      this.notify.warn('Select at least one role');
      return;
    }

    if (!this.isEdit) {
      const dto: AdminCreateUserWithAccountRequest  = {
        firstName: v.firstName!.trim(),
        middleName: v.middleName?.trim() ? v.middleName!.trim() : null,
        lastName: v.lastName!.trim(),
        email: v.email!.trim(),
        phone: v.phone?.trim() ? v.phone!.trim() : null,
        birthDate: v.birthDate?.trim() ? v.birthDate!.trim() : null,

        login: v.login!.trim(),
        password: v.password!.trim(),
        roles: v.roles ?? [],
        status: v.status ?? null,
      };

      this.adminApi.createUserWithAccount(dto)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.notify.success('User + account created');
            this.ref.close('ok');
          },
          error: (err: any) => {
            this.notify.error(err?.userMessage ?? 'Failed to create user + account');
          }
        });

      return;
    }
    const userId = this.data.user!.id;
    const acc = this.data.account;

    const userDto: UserUpdateDTO = {
      firstName: v.firstName!.trim(),
      middleName: v.middleName?.trim() ? v.middleName!.trim() : null,
      lastName: v.lastName!.trim(),
      email: v.email!.trim(),
      phone: v.phone?.trim() ? v.phone!.trim() : null,
      birthDate: v.birthDate?.trim() ? v.birthDate!.trim() : null,
    };

    // Business rule: update user first, then account status/roles.
    this.usersApi.update(userId, userDto)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          // If the user has no account, stop after user update.
          if (!acc) {
            this.notify.success('User updated');
            this.ref.close('ok');
            return;
          }

          // status + roles
          this.accountsApi.updateStatus(acc.id, v.status!).subscribe({
            next: () => {
              this.accountsApi.updateRoles(acc.id, v.roles ?? []).subscribe({
                next: () => {
                  // optional reset password
                  const pw = (v.resetPassword || '').trim();
                  if (pw) {
                    if (pw.length < 6) {
                      this.notify.warn('Password must have at least 6 chars');
                      this.ref.close('ok');
                      return;
                    }
                    this.accountsApi.changePassword(acc.id, pw).subscribe({
                      next: () => {
                        this.notify.success('User + account updated (password reset)');
                        this.ref.close('ok');
                      },
                      error: (err: any) => {
                        this.notify.warn(err?.userMessage ?? 'Updated, but password reset failed');
                        this.ref.close('ok');
                      }
                    });
                  } else {
                    this.notify.success('User + account updated');
                    this.ref.close('ok');
                  }
                },
                error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to update roles'),
              });
            },
            error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to update status'),
          });
        },
        error: (err: any) => this.notify.error(err?.userMessage ?? 'Failed to update user'),
      });
  }
  close() { this.ref.close(); }
}



