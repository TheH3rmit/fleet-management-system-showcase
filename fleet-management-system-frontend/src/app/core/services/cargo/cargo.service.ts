import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { CargoDTO, CreateCargoRequest } from '../../models/cargo.model';
import { Page } from '../../models/page.model';
import { buildHttpParams } from '../http-params.util';

export interface CargoSearchParams {
  q?: string;
  page?: number;
  size?: number;
  sort?: string[];
}

export interface UpdateCargoRequest {
  cargoDescription?: string | null;
  weightKg?: number | null;
  volumeM3?: number | null;
  pickupDate?: string | null;
  deliveryDate?: string | null;
}

@Injectable({ providedIn: 'root' })
export class CargoService {
  private http = inject(HttpClient);
  private base = '/api/cargos';
  private readonly lookupSize = 200;

  // Fetches a limited list of cargo for lookups.
  getAll(size = this.lookupSize): Observable<CargoDTO[]> {
    return this.list({ page: 0, size }).pipe(map(page => page.content));
  }

  // Fetches a cargo item by id.
  getOne(id: number): Observable<CargoDTO> {
    return this.http.get<CargoDTO>(`${this.base}/${id}`);
  }

  // Fetches cargo assigned to a transport.
  getByTransport(transportId: number): Observable<CargoDTO[]> {
    return this.http.get<CargoDTO[]>(`${this.base}/transport/${transportId}`);
  }

  // Lists cargo with search and pagination.
  list(params: CargoSearchParams): Observable<Page<CargoDTO>> {
    const httpParams = buildHttpParams({
      q: params.q,
      page: params.page,
      size: params.size,
      sort: params.sort,
    });

    return this.http.get<Page<CargoDTO>>(this.base, { params: httpParams });
  }

  // Creates a cargo item.
  create(req: CreateCargoRequest): Observable<CargoDTO> {
    return this.http.post<CargoDTO>(this.base, req);
  }

  // Creates cargo directly for a transport.
  createForTransport(
    transportId: number,
    req: Omit<CreateCargoRequest, 'transportId'>
  ): Observable<CargoDTO> {
    return this.http.post<CargoDTO>(`${this.base}/transport/${transportId}`, req);
  }


  // Updates an existing cargo item.
  update(id: number, req: UpdateCargoRequest): Observable<CargoDTO> {
    return this.http.put<CargoDTO>(`${this.base}/${id}`, req);
  }

  // Deletes a cargo item by id.
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}







