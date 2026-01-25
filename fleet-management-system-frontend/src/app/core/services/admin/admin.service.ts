import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminCreateUserWithAccountRequest, AdminCreateUserWithAccountResponseDTO } from '../../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private base = '/api/admin';

  // Creates a user with linked account in a single admin action.
  createUserWithAccount(dto: AdminCreateUserWithAccountRequest): Observable<AdminCreateUserWithAccountResponseDTO> {
    return this.http.post<AdminCreateUserWithAccountResponseDTO>(`${this.base}/users-with-account`, dto);
  }
}







