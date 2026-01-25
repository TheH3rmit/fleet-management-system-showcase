import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { VehicleStatus } from '../../models/vehicle-status.model';
import { CreateVehicleRequest, VehicleDTO } from "../../models/vehicle.model";
import { Page } from "../../models/page.model";
import { buildHttpParams } from '../http-params.util';

export interface VehicleSearchParams {
  q?: string;
  page?: number;
  size?: number;
  sort?: string[];
}


@Injectable({ providedIn: 'root' })
export class VehicleService {
  constructor(private http: HttpClient) {}
  private readonly lookupSize = 200;

  /** GET /api/vehicles */
  getAll(size = this.lookupSize): Observable<VehicleDTO[]> {
    return this.list({ page: 0, size }).pipe(map(page => page.content));
  }

  /** GET /api/vehicles/{id} */
  getOne(id: number): Observable<VehicleDTO> {
    return this.http.get<VehicleDTO>(`/api/vehicles/${id}`);
  }

  // Fetches multiple vehicles by id list.
  getManyByIds(ids: number[]): Observable<VehicleDTO[]> {
    if (!ids || ids.length === 0) {
      return new Observable<VehicleDTO[]>(subscriber => {
        subscriber.next([]);
        subscriber.complete();
      });
    }

    const params = buildHttpParams({ ids: ids.join(',') });
    return this.http.get<VehicleDTO[]>('/api/vehicles', { params });
  }

  // Lists vehicles with search and pagination.
  list(params: VehicleSearchParams): Observable<Page<VehicleDTO>> {
    const httpParams = buildHttpParams({
      q: params.q,
      page: params.page,
      size: params.size,
      sort: params.sort,
    });

    return this.http.get<Page<VehicleDTO>>('/api/vehicles', { params: httpParams });
  }

  /** PATCH /api/vehicles/{id}/status */
  // Updates vehicle status.
  changeStatus(id: number, status: VehicleStatus): Observable<VehicleDTO> {
    return this.http.patch<VehicleDTO>(`/api/vehicles/${id}/status`, { status });
  }

  /** POST /api/vehicles - vehicle creation */
  // Creates a vehicle.
  createVehicle(payload: CreateVehicleRequest): Observable<VehicleDTO> {
    return this.http.post<VehicleDTO>('/api/vehicles', payload);
  }

  // Fetches vehicles that are currently available.
  getAvailable() {
    return this.http.get<VehicleDTO[]>('/api/vehicles/available');
  }

  // Updates vehicle details.
  updateVehicle(id: number, payload: CreateVehicleRequest): Observable<VehicleDTO> {
    return this.http.put<VehicleDTO>(`/api/vehicles/${id}`, payload);
  }

  // Deletes a vehicle by id.
  deleteVehicle(id: number): Observable<void> {
    return this.http.delete<void>(`/api/vehicles/${id}`);
  }
}







