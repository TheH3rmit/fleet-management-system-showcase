// src/app/core/services/location/location.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { CreateLocationRequest, LocationDTO } from '../../models/location.model';
import { Page } from '../../models/page.model';
import { buildHttpParams } from '../http-params.util';

export interface LocationSearchParams {
  q?: string;
  page?: number;
  size?: number;
  sort?: string[];
}


@Injectable({ providedIn: 'root' })
export class LocationService {
  constructor(private http: HttpClient) {}
  private readonly lookupSize = 500;

  // Fetches a limited list of locations for lookups.
  getAll(size = this.lookupSize): Observable<LocationDTO[]> {
    return this.list({ page: 0, size }).pipe(map(page => page.content));
  }

  // Fetches a single location by id.
  getOne(id: number): Observable<LocationDTO> {
    return this.http.get<LocationDTO>(`/api/locations/${id}`);
  }

  // Creates a new location.
  create(req: CreateLocationRequest): Observable<LocationDTO> {
    return this.http.post<LocationDTO>('/api/locations', req);
  }

  // Updates an existing location.
  update(id: number, req: CreateLocationRequest): Observable<LocationDTO> {
    return this.http.put<LocationDTO>(`/api/locations/${id}`, req);
  }

  // Deletes a location by id.
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/locations/${id}`);
  }

  // Lists locations with search and pagination.
  list(params: LocationSearchParams): Observable<Page<LocationDTO>> {
    const httpParams = buildHttpParams({
      q: params.q,
      page: params.page,
      size: params.size,
      sort: params.sort,
    });

    return this.http.get<Page<LocationDTO>>('/api/locations', { params: httpParams });
  }
}







