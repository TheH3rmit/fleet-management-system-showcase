import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AccountResponseDTO, AccountUserViewDTO } from '../../models/account.model';
import {
  AccountRegisterDTO,
  AccountStatusUpdateDTO,
  AccountRolesUpdateDTO,
  AccountAdminResetPasswordDTO
} from '../../models/account.model';
import { buildHttpParams } from '../http-params.util';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private http = inject(HttpClient);
  private base = '/api/accounts';

  // Registers a new account for an existing user.
  register(dto: AccountRegisterDTO): Observable<AccountResponseDTO> {
    return this.http.post<AccountResponseDTO>(this.base, dto);
  }

  // Fetches an account by id.
  get(id: number): Observable<AccountResponseDTO> {
    return this.http.get<AccountResponseDTO>(`${this.base}/${id}`);
  }

  // Fetches account details with linked user data.
  view(id: number): Observable<AccountUserViewDTO> {
    return this.http.get<AccountUserViewDTO>(`${this.base}/${id}/details`);
  }

  // Looks up an account by login (query param).
  findByLogin(login: string): Observable<AccountResponseDTO> {
    const params = buildHttpParams({ q: login });
    return this.http.get<AccountResponseDTO>(`${this.base}`, {
      params
    });
  }

  // Resets password as an admin action.
  changePassword(id: number, newPassword: string): Observable<void> {
    const body: AccountAdminResetPasswordDTO = { newPassword };
    return this.http.patch<void>(`${this.base}/${id}/password`, body);
  }

  // Updates account status.
  updateStatus(id: number, status: AccountStatusUpdateDTO['status']): Observable<void> {
    const body: AccountStatusUpdateDTO = { status };
    return this.http.patch<void>(`${this.base}/${id}/status`, body);
  }

  // Updates account roles.
  updateRoles(id: number, roles: AccountRolesUpdateDTO['roles']): Observable<void> {
    const body: AccountRolesUpdateDTO = { roles };
    return this.http.patch<void>(`${this.base}/${id}/roles`, body);
  }

}







