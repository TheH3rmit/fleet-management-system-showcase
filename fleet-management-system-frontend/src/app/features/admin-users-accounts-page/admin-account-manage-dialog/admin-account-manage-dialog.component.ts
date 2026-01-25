import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

import { AccountResponseDTO } from '../../../core/models/account.model';
import { UserResponseDTO } from '../../../core/models/user.model';

@Component({
  selector: 'app-account-manage-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
  ],
  templateUrl: './admin-account-manage-dialog.component.html',
  styleUrl: './admin-account-manage-dialog.component.scss'
})
export class AdminAccountManageDialogComponent  {
  private ref = inject(MatDialogRef<AdminAccountManageDialogComponent>);

  data = inject(MAT_DIALOG_DATA) as {
    account: AccountResponseDTO;
    user?: UserResponseDTO;
  };

  account = signal<AccountResponseDTO>(this.data.account);

  close() {
    this.ref.close();
  }
}



