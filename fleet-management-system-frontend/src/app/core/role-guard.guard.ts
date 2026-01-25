import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from './services/auth/auth.service';
import { UserRole } from "./models/user-role.model";

export const roleGuardGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = (route.data?.['roles'] as UserRole[] | undefined) ?? [];

  if (!auth.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }
  if (allowedRoles.length === 0) {
    return true;
  }

  // if user has correct role
  if (allowedRoles.some(r => auth.hasRole(r))) {
    return true;
  }

  // if user does not have the correct role
  router.navigate(['/menu']);
  return false;
};



