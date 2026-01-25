import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { TrailerStatus } from '../../models/trailer-status.model';
import { CreateTrailerRequest, TrailerDTO } from "../../models/trailer.model";
import { Page } from "../../models/page.model";
import { buildHttpParams } from '../http-params.util';

export interface TrailerSearchParams {
  q?: string;
  page?: number;
  size?: number;
  sort?: string[];
}

@Injectable({ providedIn: 'root' })
export class TrailerService {
  constructor(private http: HttpClient) {}
  private readonly lookupSize = 200;

  // Fetches a limited list of trailers for lookups.
  getAll(size = this.lookupSize): Observable<TrailerDTO[]> {
    return this.list({ page: 0, size }).pipe(map(page => page.content));
  }

  // Fetches a trailer by id.
  getOne(id: number): Observable<TrailerDTO> {
    return this.http.get<TrailerDTO>(`/api/trailers/${id}`);
  }

  // Lists trailers with search and pagination.
  list(params: TrailerSearchParams): Observable<Page<TrailerDTO>> {
    const httpParams = buildHttpParams({
      q: params.q,
      page: params.page,
      size: params.size,
      sort: params.sort,
    });

    return this.http.get<Page<TrailerDTO>>('/api/trailers', { params: httpParams });
  }

  // Creates a trailer.
  createTrailer(req: CreateTrailerRequest): Observable<TrailerDTO> {
    return this.http.post<TrailerDTO>('/api/trailers', req);
  }

  // Fetches trailers that are currently available.
  getAvailable() {
    return this.http.get<TrailerDTO[]>('/api/trailers/available');
  }

  // Updates trailer status.
  changeStatus(id: number, status: TrailerStatus): Observable<TrailerDTO> {
    return this.http.patch<TrailerDTO>(`/api/trailers/${id}/status`, { status });
  }

  // Updates trailer details.
  updateTrailer(id: number, payload: CreateTrailerRequest): Observable<TrailerDTO> {
    return this.http.put<TrailerDTO>(`/api/trailers/${id}`, payload);
  }

  // Deletes a trailer by id.
  deleteTrailer(id: number): Observable<void> {
    return this.http.delete<void>(`/api/trailers/${id}`);
  }
}







