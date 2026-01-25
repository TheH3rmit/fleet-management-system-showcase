import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from './services/auth/auth.service';
import { catchError, map } from 'rxjs/operators';

export const authGuardGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // already authenticated
  if (auth.isAuthenticated()) {
    return true;
  }

  // no token -> redirect to login
  const token = auth.token() ?? localStorage.getItem('access_token');
  if (!token) {
    return router.createUrlTree(['/login']);
  }

  // token present -> try to fetch /api/me
  return auth.loadMe().pipe(
    map(() => auth.isAuthenticated()
      ? true
      : router.createUrlTree(['/login'])
    ),
    catchError(() => {
      return [router.createUrlTree(['/login'])];
    })
  );
};



