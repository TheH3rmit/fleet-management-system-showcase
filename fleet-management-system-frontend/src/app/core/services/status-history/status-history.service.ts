import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StatusHistoryDTO } from '../../models/status-history.model';

@Injectable({
  providedIn: 'root'
})
export class StatusHistoryService {
  constructor(private http: HttpClient) {}

  // Fetches status history for a transport.
  getByTransportId(transportId: number): Observable<StatusHistoryDTO[]> {
    return this.http.get<StatusHistoryDTO[]>(`/api/transports/${transportId}/history`);
  }

}







