import { TransportStatus } from './transport-status.model';

export interface StatusHistoryDTO {
  id: number;
  transportId: number;
  status: TransportStatus;
  changedAt: string;     // ISO
  changedBy?: number | null;   // userId
  changedByName?: string | null;
}



