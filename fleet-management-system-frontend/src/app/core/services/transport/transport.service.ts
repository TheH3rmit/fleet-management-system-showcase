import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import {
  TransportDTO,
  CreateTransportRequest,
  UpdateTransportStatusRequest,
  TransportDetailsDTO
} from '../../models/transport.model';
import { TransportStatus } from '../../models/transport-status.model';
import { Page } from "../../models/page.model";
import { buildHttpParams } from '../http-params.util';

export interface TransportSearchParams {
  status?: TransportStatus;
  driverId?: number;
  vehicleId?: number;
  q?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}


@Injectable({
  providedIn: 'root'
})
export class TransportService {

  constructor(private http: HttpClient) {}
  private readonly lookupSize = 200;

  // Fetches a limited list of transports for lookups.
  getAll(): Observable<TransportDTO[]> {
    return this.list({ page: 0, size: this.lookupSize }).pipe(map(page => page.content));
  }

  // Fetches a transport by id.
  getById(id: number): Observable<TransportDTO> {
    return this.http.get<TransportDTO>(`/api/transports/${id}`);
  }

  // Creates a transport.
  create(req: CreateTransportRequest): Observable<TransportDTO> {
    return this.http.post<TransportDTO>('/api/transports', req);
  }

  // Updates a transport (dispatcher/admin).
  update(id: number, req: CreateTransportRequest): Observable<TransportDTO> {
    return this.http.put<TransportDTO>(`/api/transports/${id}`, req);
  }

  // Updates a transport as admin.
  adminUpdate(id: number, dto: CreateTransportRequest) {
    return this.http.put<TransportDTO>(`/api/transports/${id}/admin`, dto);
  }

  // Deletes a transport by id.
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/transports/${id}`);
  }

  // ADMIN/DISPATCHER (changedBy is taken from auth, no params)

  // Cancels a transport.
  cancel(id: number): Observable<TransportDTO> {
    return this.updateStatus(id, TransportStatus.CANCELLED);
  }

  // Marks a transport as failed.
  fail(id: number): Observable<TransportDTO> {
    return this.updateStatus(id, TransportStatus.FAILED);
  }

  // Rejects a transport.
  reject(id: number): Observable<TransportDTO> {
    return this.updateStatus(id, TransportStatus.REJECTED);
  }

  // Assigns a driver to a transport.
  assignDriver(transportId: number, driverId: number): Observable<TransportDTO> {
    return this.http.patch<TransportDTO>(`/api/transports/${transportId}/assign-driver/${driverId}`, null);
  }

  // --- driver actions ---

  // Fetches detailed transport data.
  getDetails(id: number): Observable<TransportDetailsDTO> {
    return this.http.get<TransportDetailsDTO>(`/api/transports/${id}/details`);
  }

  // Driver accepts a transport.
  accept(id: number): Observable<TransportDTO> {
    return this.updateStatus(id, TransportStatus.ACCEPTED);
  }

  // Driver starts a transport.
  start(id: number): Observable<TransportDTO> {
    return this.updateStatus(id, TransportStatus.IN_PROGRESS);
  }

  // Driver finishes a transport.
  finish(id: number): Observable<TransportDTO> {
    return this.updateStatus(id, TransportStatus.FINISHED);
  }

  // Updates transport status.
  updateStatus(id: number, status: TransportStatus): Observable<TransportDTO> {
    const body: UpdateTransportStatusRequest = { status };
    return this.http.patch<TransportDTO>(`/api/transports/${id}/status`, body);
  }
  // Lists transports assigned to a driver.
  getByDriver(driverId: number): Observable<TransportDTO[]> {
    return this.http.get<TransportDTO[]>(`/api/transports/driver/${driverId}`);
  }

  // Lists transports with filters and pagination.
  list(params: TransportSearchParams): Observable<Page<TransportDTO>> {
    const httpParams = buildHttpParams({
      status: params.status,
      driverId: params.driverId,
      vehicleId: params.vehicleId,
      q: params.q,
      from: params.from,
      to: params.to,
      page: params.page,
      size: params.size,
    });

    return this.http.get<Page<TransportDTO>>('/api/transports', { params: httpParams });
  }
}







