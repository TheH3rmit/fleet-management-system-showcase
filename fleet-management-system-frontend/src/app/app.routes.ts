import { Routes } from '@angular/router';
import { LoginComponent } from "./features/login/login.component";
import { ShellComponent } from "./layout/shell/shell.component";
import { MenuPageComponent } from "./features/menu-page/menu-page.component";
import { TransportsPageComponent } from "./features/transports-page/transports-page.component";
import { AccountPageComponent } from "./features/account-page/account-page.component";
import { authGuardGuard } from './core/auth-guard.guard';
import { roleGuardGuard } from './core/role-guard.guard';
import { AssetsPageComponent } from "./features/assets-page/assets-page.component";
import { DriversManagePageComponent } from "./features/drivers-manage-page/drivers-manage-page.component";
import { DriversPageComponent } from "./features/drivers-page/drivers-page.component";
import { UserRole } from "./core/models/user-role.model";
import { AdminUsersAccountsPageComponent } from "./features/admin-users-accounts-page/admin-users-accounts-page.component";
import { LocationManagePageComponent } from "./features/location-page/location-manage-page/location-manage-page.component";
import { CargosManageComponent } from "./features/cargo-page/cargos-manage/cargos-manage.component";
import { WorkLogManageComponent } from "./features/worklog-page/work-log-manage/work-log-manage.component";

export const routes: Routes = [
  // Public routes
  { path: 'login', component: LoginComponent },

  // App layout - only for authenticated users below
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuardGuard],
    children: [
      { path: '', redirectTo: 'menu', pathMatch: 'full' },

      { path: 'menu', component: MenuPageComponent },

      // Dispatcher view
      {
        path: 'transports',
        component: TransportsPageComponent,
        canActivate: [roleGuardGuard],
        data: { roles: [UserRole.DISPATCHER] },
      },
      {
        path: 'assets',
        component: AssetsPageComponent,
        canActivate: [roleGuardGuard],
        data: { roles: [UserRole.DISPATCHER] },
      },
      {
        path: 'drivers-manage',
        component: DriversManagePageComponent,
        canActivate: [roleGuardGuard],
        data: { roles: [UserRole.DISPATCHER] },
      },
      {
        path: 'cargos',
        component: CargosManageComponent,
        canActivate: [roleGuardGuard],
        data: { roles: [UserRole.DISPATCHER, UserRole.ADMIN] },
      },
      {
        path: 'worklog',
        component: WorkLogManageComponent,
        canActivate: [roleGuardGuard],
        data: { roles: [UserRole.DISPATCHER, UserRole.ADMIN, UserRole.DRIVER] },
      },

      // Admin view
      {
        path: 'users',
        component: AdminUsersAccountsPageComponent,
        canActivate: [roleGuardGuard],
        data: { roles: [UserRole.ADMIN] },
      },
      {
        path: 'locations',
        component: LocationManagePageComponent,
        canActivate: [roleGuardGuard],
        data: { roles: [UserRole.DISPATCHER, UserRole.ADMIN] },
      },

      // Driver view
      {
        path: 'drivers',
        component: DriversPageComponent,
        canActivate: [roleGuardGuard],
        data: { roles: [UserRole.DRIVER] },
      },

      // Shared view
      { path: 'account', component: AccountPageComponent },
    ],
  },

  // Fallback
  { path: '**', redirectTo: 'menu' }
];



