import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

type NoticeKind = 'success' | 'info' | 'warn' | 'error';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private lastMsg?: string;
  private lastKind?: NoticeKind;
  private lastAt = 0;

  //how much ms blocking identical communication message
  private readonly dedupeWindowMs = 1500;

  constructor(private snack: MatSnackBar) {}

  // Shows a snackbar notification with basic de-duplication.
  show(message: string, kind: NoticeKind = 'info', durationMs = 3500) {
    const now = Date.now();

    // anti-spam: same message same type in the same time
    if (
      this.lastMsg === message &&
      this.lastKind === kind &&
      now - this.lastAt < this.dedupeWindowMs
    ) {
      return;
    }

    this.lastMsg = message;
    this.lastKind = kind;
    this.lastAt = now;

    // dismiss the previous one to unload queue
    this.snack.dismiss();

    this.snack.open(message, 'OK', {
      duration: durationMs,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: [`snack-${kind}`],
    });
  }

  // Shows a success notification.
  success(msg: string) { this.show(msg, 'success'); }
  // Shows an info notification.
  info(msg: string)    { this.show(msg, 'info'); }
  // Shows a warning notification.
  warn(msg: string)    { this.show(msg, 'warn'); }
  // Shows an error notification with a longer timeout.
  error(msg: string)   { this.show(msg, 'error', 5000); }
}







