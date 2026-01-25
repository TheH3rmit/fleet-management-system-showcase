import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export type ConfirmDialogData = {
  title?: string;
  message: string;

  confirmText?: string;  // default: "Confirm"
  cancelText?: string;   // default: "Cancel"

  icon?: string;         // default: "warning"
  confirmColor?: 'primary' | 'accent' | 'warn'; // default: "warn"
};

@Component({
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './dialog-confirm.component.html',
  styleUrl: './dialog-confirm.component.scss'
})
export class ConfirmDialogComponent {
  private ref = inject(MatDialogRef<ConfirmDialogComponent, boolean>);
  data = inject(MAT_DIALOG_DATA) as ConfirmDialogData;

  close(v: boolean) {
    this.ref.close(v);
  }
}



