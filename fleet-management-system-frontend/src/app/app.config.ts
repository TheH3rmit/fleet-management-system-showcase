import { ApplicationConfig, APP_INITIALIZER, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

import { routes } from './app.routes';
import { AuthService } from './core/services/auth/auth.service';
import { authInterceptor } from './core/http/auth.interceptor';
import { apiInterceptor } from './core/http/api.interceptor';
import { httpErrorInterceptor } from './core/http/http-error.interceptor';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';

export function initAuth(auth: AuthService) {
  return () => auth.bootstrap();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimations(),
    importProvidersFrom(MatSnackBarModule, MatButtonModule),

    {
      provide: APP_INITIALIZER,
      useFactory: initAuth,
      deps: [AuthService],
      multi: true,
    },

    provideHttpClient(
      withInterceptors([
        authInterceptor,
        apiInterceptor,
        httpErrorInterceptor,
      ])
    ),
  ]
};



