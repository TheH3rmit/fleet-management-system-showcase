import { TrailerStatus } from "./trailer-status.model";

export interface TrailerDTO {
  id: number;
  name: string;
  licensePlate: string;
  payload: number | null;
  volume: number | null;
  trailerStatus: TrailerStatus;
  assignedToTransport?: boolean;
  inProgressAssigned?: boolean;
}

export interface CreateTrailerRequest {
  name: string;
  licensePlate: string;
  payload: number | null;
  volume: number | null;
}



