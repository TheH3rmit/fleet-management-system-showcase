import { HttpInterceptorFn } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {


  return next(req).pipe(
    catchError(err => {
      // handling of http statuses
      const backendMsg = err?.error?.message ?? err?.error?.error;
      const msg =
        err.status === 0   ? 'Backend is unavailable.'
          : err.status === 400 ? (backendMsg ?? 'Validation error')
            : err.status === 401 ? 'Unauthorized — please sign in.'
              : err.status === 403 ? 'Forbidden — insufficient permissions.'
                : err.status === 404 ? 'Not found.'
                  : err.status === 409 ? (backendMsg ?? 'Conflict.')
                    : err.status >= 500 ? 'Server error.'
                      : 'Unknown error.';

      return throwError(() => ({ ...err, userMessage: msg }));
    })
  );
};



