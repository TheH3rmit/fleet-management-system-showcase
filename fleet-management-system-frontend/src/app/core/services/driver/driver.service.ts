import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, of, shareReplay, tap } from 'rxjs';
import { DriverStatus } from '../../models/driver-status.model';
import { CreateDriverRequest, DriverDTO, UpdateDriverRequest } from "../../models/driver.model";
import { CargoDTO } from "../../models/cargo.model";
import { StatusHistoryDTO } from "../../models/status-history.model";
import { TransportDTO } from "../../models/transport.model";
import { Page } from "../../models/page.model";
import { buildHttpParams } from '../http-params.util';

export interface DriverSearchParams {
  q?: string;
  page?: number;
  size?: number;
  sort?: string[];
}


@Injectable({
  providedIn: 'root'
})
export class DriverService {
  constructor(private http: HttpClient) {}

  private readonly lookupSize = 200;
  private allCache$?: Observable<DriverDTO[]>;
  private allSnapshot: DriverDTO[] | null = null;

  // Fetches drivers with optional cached lookup list.
  getAll(size = this.lookupSize): Observable<DriverDTO[]> {
    if (size !== this.lookupSize) {
      return this.list({ page: 0, size }).pipe(map(page => page.content));
    }

    if (!this.allCache$) {
      this.allCache$ = this.list({ page: 0, size: this.lookupSize }).pipe(
        map(page => page.content),
        tap(list => (this.allSnapshot = list ?? [])),
        shareReplay(1)
      );
    }
    return this.allCache$;
  }

  // Clears cached driver list.
  invalidateCache() {
    this.allCache$ = undefined;
    this.allSnapshot = null;
  }

  // Returns a map of cached drivers keyed by userId.
  getCachedMap(): Record<number, DriverDTO> {
    const map: Record<number, DriverDTO> = {};
    for (const d of this.allSnapshot ?? []) map[d.userId] = d;
    return map;
  }

  // Fetches a driver by id.
  getOne(id: number): Observable<DriverDTO> {
    return this.http.get<DriverDTO>(`/api/drivers/${id}`);
  }

  // Lists drivers with search and pagination.
  list(params: DriverSearchParams): Observable<Page<DriverDTO>> {
    const httpParams = buildHttpParams({
      q: params.q,
      page: params.page,
      size: params.size,
      sort: params.sort,
    });

    return this.http.get<Page<DriverDTO>>('/api/drivers', { params: httpParams });
  }

  // Fetches drivers that are currently available.
  getAvailable() {
    return this.http.get<DriverDTO[]>('/api/drivers/available');
  }

  // Creates a driver profile for a user.
  create(req: CreateDriverRequest): Observable<DriverDTO> {
    return this.http.post<DriverDTO>('/api/drivers', req);
  }

  // Updates driver details.
  update(userId: number, req: UpdateDriverRequest): Observable<DriverDTO> {
    return this.http.put<DriverDTO>(`/api/drivers/${userId}`, req);
  }

  // Deletes a driver by user id.
  delete(userId: number): Observable<void> {
    return this.http.delete<void>(`/api/drivers/${userId}`);
  }

  // Fetches multiple drivers by id list.
  getManyByIds(ids: number[]): Observable<DriverDTO[]> {
    if (!ids || ids.length === 0) {
      return new Observable<DriverDTO[]>(subscriber => {
        subscriber.next([]);
        subscriber.complete();
      });
    }

    const params = buildHttpParams({ ids: ids.join(',') });
    return this.http.get<DriverDTO[]>('/api/drivers', { params });
  }



  /** PATCH /api/drivers/{userId}/status */
  // Updates driver status.
  changeStatus(userId: number, status: DriverStatus): Observable<DriverDTO> {
    return this.http.patch<DriverDTO>(
      `/api/drivers/${userId}/status`,
      { status }
    );
  }

  // --- driver self-services ---

  /** GET /api/drivers/my-cargo */
  // Fetches cargo for the current driver.
  getMyCargo(): Observable<CargoDTO[]> {
    return this.http.get<CargoDTO[]>('/api/drivers/my-cargo');
  }

  /** GET /api/drivers/my-transports/timeline */
  // Fetches status timeline for current driver transports.
  getMyTimeline(): Observable<StatusHistoryDTO[]> {
    return this.http.get<StatusHistoryDTO[]>('/api/drivers/my-transports/timeline');
  }

  // Fetches current driver transports.
  getMyTransports(): Observable<TransportDTO[]> {
    return this.http.get<TransportDTO[]>('/api/drivers/my-transports');
  }
}







