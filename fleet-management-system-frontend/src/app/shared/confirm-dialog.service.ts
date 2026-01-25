import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { ConfirmDialogComponent, ConfirmDialogData } from './dialog-confirm/dialog-confirm.component';

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private dialog = inject(MatDialog);

  confirm(data: ConfirmDialogData, width = '420px'): Observable<boolean> {
    return this.dialog
      .open(ConfirmDialogComponent, { width, data })
      .afterClosed();
  }
}



