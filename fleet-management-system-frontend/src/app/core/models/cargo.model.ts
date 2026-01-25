import { TransportStatus } from "./transport-status.model";

export interface CargoDTO {
  id: number;
  cargoDescription: string;
  weightKg: number;
  volumeM3: number;
  pickupDate?: string | null;
  deliveryDate?: string | null;
  transportId?: number | null;
  transportStatus?: TransportStatus | null;
}

export interface CreateCargoRequest {
  cargoDescription: string;
  weightKg: number;
  volumeM3: number;
  pickupDate?: string | null;
  deliveryDate?: string | null;
  transportId: number;
}



