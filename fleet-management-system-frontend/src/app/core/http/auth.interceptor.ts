import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

let refreshInProgress  = false;
const refreshToken$ = new BehaviorSubject<string | null>(null);

function isAuthEndpoint(url: string) {
  return url.startsWith('/api/auth/');
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (environment.useMockAuth) {
    return next(req);
  }

  const shouldAttachToken = req.url.startsWith('/') && !isAuthEndpoint(req.url);
  let cloned = req;
  const token = auth.token();
  if (token && shouldAttachToken) {
    cloned = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(cloned).pipe(
    catchError(err => {
      // 403: routing
      if (err.status === 403) {
        router.navigateByUrl('/forbidden');
        return throwError(() => err);
      }
      // 401: refresh (not on auth endpoint)
      if (err.status !== 401 || isAuthEndpoint(req.url)) {
        return throwError(() => err);
      }


      if (refreshInProgress) {
        return refreshToken$.pipe(
          filter((t): t is string => !!t),
          take(1),
          switchMap(newToken => {
            const retried = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}`}
            });
            return next(retried);
          })
        );
      }

      // start refresh
      refreshInProgress = true;
      refreshToken$.next(null);


      return auth.refresh().pipe(
        switchMap(() => {
          refreshInProgress = false;

          const newToken = auth.token();
          if (!newToken) {
            auth.logout();
            router.navigateByUrl('/login');
            return throwError(() => err);
          }

          refreshToken$.next(newToken);

          const retried = req.clone({
            setHeaders: { Authorization: `Bearer ${newToken}` }
          });
          return next(retried);
        }),
          catchError(refreshErr => {
            refreshInProgress = false;
            refreshToken$.next(null);
            auth.logout();
            router.navigateByUrl('/login');
            return throwError(() => refreshErr);
          })
        );
    })
  );
};



