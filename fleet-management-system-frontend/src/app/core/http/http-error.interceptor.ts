import { HttpInterceptorFn } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { NotificationService } from '../services/notification/notification.service';
import { inject } from "@angular/core";

function extractBackendMessage(err: any): string | undefined {
  const e = err?.error;

  // backend returns string in err.error
  if (typeof e === 'string' && e.trim().length) return e;

  // backend
  const candidates = [
    e?.message,
    e?.error,       //  { error: "Conflict" }
    err?.message,   //  HttpErrorResponse.message
  ];

  for (const c of candidates) {
    if (typeof c === 'string' && c.trim().length) return c;
  }

  return undefined;
}

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const notify = inject(NotificationService);

  return next(req).pipe(
    catchError((err: any) => {
      const status = err?.status;

      // authInterceptor handles 401/403 (refresh/redirect) - no spam
      if (status !== 401 && status !== 403) {
        const msg =
          err?.userMessage ??
          extractBackendMessage(err) ??
          'An error occurred.';

        notify.error(msg);
      }
      return throwError(() => err);
    })
  );
};



