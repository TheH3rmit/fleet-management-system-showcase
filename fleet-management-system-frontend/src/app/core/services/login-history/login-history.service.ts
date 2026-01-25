import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { LoginHistoryDTO } from '../../models/login-history.model';

@Injectable({ providedIn: 'root' })
export class LoginHistoryService {
  private http = inject(HttpClient);
  private base = '/api/login-histories';

  // Fetches login history for the current account.
  getMy(): Observable<LoginHistoryDTO[]> {
    return this.http.get<LoginHistoryDTO[]>(`${this.base}/me`);
  }

  // Fetches login history for a specific account (admin only).
  getByAccount(accountId: number): Observable<LoginHistoryDTO[]> {
    return this.http.get<LoginHistoryDTO[]>(`${this.base}/account/${accountId}`);
  }
}
