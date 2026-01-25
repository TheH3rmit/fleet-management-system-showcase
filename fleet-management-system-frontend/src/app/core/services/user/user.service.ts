import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserCreateDTO, UserResponseDTO, UserUpdateDTO } from '../../models/user.model';
import { Observable } from 'rxjs';
import { Page } from '../../models/page.model';
import { map } from "rxjs/operators";
import { buildHttpParams } from '../http-params.util';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private base = '/api/users';


  // Creates a new user.
  create(dto: UserCreateDTO): Observable<UserResponseDTO> {
    return this.http.post<UserResponseDTO>(this.base, dto);
  }

  /** Light: GET /api/users/{id} (default without graph) */
  getOne(id: number): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.base}/${id}`);
  }

  /** Heavy/details: GET /api/users/{id}?withGraph=true */
  getDetails(id: number): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.base}/${id}/details`);
  }
  
  // Lists users with search and pagination.
  list(opts: { q?: string; page?: number; size?: number; sort?: string }): Observable<Page<UserResponseDTO>> {
    const params = buildHttpParams({
      q: opts.q,
      page: opts.page,
      size: opts.size,
      sort: opts.sort,
    });

    return this.http.get<any>(this.base, { params }).pipe(
      map((raw) => ({
        content: raw?.content ?? [],
        totalElements: raw?.totalElements ?? 0,
        totalPages: raw?.totalPages ?? 0,
        size: raw?.size ?? opts.size ?? 10,
        number: raw?.number ?? opts.page ?? 0,
      }))
    );
  }

  // Partially updates user data.
  update(id: number, dto: UserUpdateDTO): Observable<UserResponseDTO> {
    return this.http.patch<UserResponseDTO>(`${this.base}/${id}`, dto);
  }

  // Deletes a user by id.
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // Convenience search that returns only the content array.
  search(q: string, size = 10): Observable<UserResponseDTO[]> {
    return this.list({ q, page: 0, size })
      .pipe(map((p) => p.content ?? []));
  }
}







