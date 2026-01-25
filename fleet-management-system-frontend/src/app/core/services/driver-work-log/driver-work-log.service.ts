import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateDriverWorkLogRequest,
  DriverWorkLogDTO,
  UpdateDriverWorkLogRequest,
} from '../../models/driver-work-log.model';

@Injectable({ providedIn: 'root' })
export class DriverWorkLogService {
  private http = inject(HttpClient);
  private base = '/api/work-logs';

  // Fetches all work log entries.
  listAll(): Observable<DriverWorkLogDTO[]> {
    return this.http.get<DriverWorkLogDTO[]>(this.base);
  }

  // Fetches work log entries for a specific driver.
  listByDriver(driverId: number): Observable<DriverWorkLogDTO[]> {
    return this.http.get<DriverWorkLogDTO[]>(`${this.base}/driver/${driverId}`);
  }

  // Fetches work log entries for the current driver.
  listMy(): Observable<DriverWorkLogDTO[]> {
    return this.http.get<DriverWorkLogDTO[]>(`${this.base}/my`);
  }

  // Creates a work log entry as admin/dispatcher.
  create(req: CreateDriverWorkLogRequest): Observable<DriverWorkLogDTO> {
    return this.http.post<DriverWorkLogDTO>(this.base, req);
  }

  // Creates a work log entry for the current driver.
  createMy(req: CreateDriverWorkLogRequest): Observable<DriverWorkLogDTO> {
    return this.http.post<DriverWorkLogDTO>(`${this.base}/my`, req);
  }

  // Updates an existing work log entry.
  update(id: number, req: CreateDriverWorkLogRequest): Observable<DriverWorkLogDTO> {
    return this.http.put<DriverWorkLogDTO>(`${this.base}/${id}`, req);
  }

  // Deletes a work log entry by id.
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}







