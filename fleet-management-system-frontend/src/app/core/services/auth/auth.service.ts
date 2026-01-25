import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, throwError, delay, scheduled, asyncScheduler, switchMap, catchError, of } from 'rxjs'
import { environment } from '../../../../environments/environment';

export interface MeAccountShort {
  accountId: number;
  login: string;
  status: string;
  createdAt: string | null;
  lastLoginAt: string | null;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
}
export interface MeDto {
  authenticated: boolean;
  username?: string;
  roles?: string[]; // ex ["ROLE_ADMIN"]
  account?: MeAccountShort;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  // signals (app state)
  token = signal<string | null>(null);
  me    = signal<MeDto | null>(null);

  constructor(private http: HttpClient) {}

  /** On app boot up ( in AppComponent) */
  bootstrap() {
    const access = localStorage.getItem('access_token');
    if (!access) {
      this.token.set(null);
      this.me.set({ authenticated: false });
      return;
    }

    this.token.set(access);

    this.loadMe().pipe(
      catchError(() => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        this.token.set(null);
        this.me.set({ authenticated: false });
        return scheduled([{ authenticated: false } as MeDto], asyncScheduler);
      })
    ).subscribe();
  }

  /** login - save to localStorage */
  /** login — works in prod with backend and with dev with mock */
  login(login: string, password: string) {
    if (environment.useMockAuth) {
      // MOCK for dev when no backend
      if (login === 'admin' && password === 'admin') {
        const fakeToken = 'mock-token-admin';
        const mockMe: MeDto = {
          authenticated: true,
          username: 'admin',
          roles: ['ADMIN', 'DISPATCHER']
        };
        this.setMockSession(fakeToken, mockMe);
        return scheduled([mockMe], asyncScheduler).pipe(delay(200));
      }

      if (login === 'driver' && password === 'driver') {
        const fakeToken = 'mock-token-driver';
        const mockMe: MeDto = {
          authenticated: true,
          username: 'driver',
          roles: ['DRIVER']
        };
        this.setMockSession(fakeToken, mockMe);
        return scheduled([mockMe], asyncScheduler).pipe(delay(200));
      }

      return throwError(() => ({ error: { message: 'Invalid credentials' } }));
    }

    // PROD login
    return this.http.post<{ accessToken: string; refreshToken: string }>('/api/auth/login', { login, password }).pipe(
      tap(res => {
        localStorage.setItem('access_token', res.accessToken);
        localStorage.setItem('refresh_token', res.refreshToken);
        this.token.set(res.accessToken);
      }),
      switchMap(() => this.loadMe())
    );
  }

  // Requests a new access token using the refresh token.
  refresh() {
    const refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) return throwError(() => new Error('Missing refresh token'));

    return this.http.post<{ accessToken: string; refreshToken: string }>('/api/auth/refresh', { refreshToken }).pipe(
      tap(res => {
        localStorage.setItem('access_token', res.accessToken);
        localStorage.setItem('refresh_token', res.refreshToken);
        this.token.set(res.accessToken);
      })
    );
  }

  // Clears session and logs out from the backend when possible.
  logout() {
    const token = this.token(); // take from signal not from local storage

    if (!environment.useMockAuth && token) {
      // send token manually interceptor does not add /api/auth/**
      this.http.post('/api/auth/logout', null, {
        headers: { Authorization: `Bearer ${token}` }
      }).subscribe({
        next: () => console.log('Logged out'),
        error: () => console.warn('Logout failed (token probably expired)')
      });
    }

    // clear localStorage at logout
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    this.token.set(null);
    this.me.set({ authenticated: false, roles: [], account: undefined, username: undefined });
  }

  /** user data loading */
  loadMe() {
    if (environment.useMockAuth) {
      // MOCK: API /api/me
      const mockData = localStorage.getItem('mock_me');
      if (mockData) {
        const parsed: MeDto = JSON.parse(mockData);
        this.me.set(parsed);
        return scheduled([parsed], asyncScheduler).pipe(delay(200));
      }
      return scheduled([{authenticated: false}], asyncScheduler).pipe(delay(200));
    }

    // PROD
    return this.http.get<MeDto>('/api/me').pipe(
      tap(m => this.me.set(m))
    );
  }

  //Utility
  private setMockSession(token: string, mockMe: MeDto) {
    localStorage.setItem('access_token', token);
    this.token.set(token);
    this.me.set(mockMe);
    localStorage.setItem('mock_me', JSON.stringify(mockMe));
  }

  // Returns true when the current session is authenticated.
  isAuthenticated(): boolean {
    return this.me()?.authenticated === true;
  }

  // Checks if current user has a given role.
  hasRole(role: string): boolean {
    const roles = this.me()?.roles ?? [];
    return roles.some(r => r === role || r === `ROLE_${role}`);
  }
}







