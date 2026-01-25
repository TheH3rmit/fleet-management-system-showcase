import { TransportStatus } from "./transport-status.model";
import { Page } from "./page.model";
import { LocationDTO } from "./location.model";

/* -----------------------------------------
  Transport Data Transfer Objects (DTOs)
------------------------------------------ */
export interface TransportDTO {
  id: number;


  driverId?: number | null;
  vehicleId: number;
  trailerId: number;
  vehicleLabel?: string | null;
  trailerLabel?: string | null;

  createdById: number;

  contractualDueAt?: string | null;
  plannedStartAt?: string | null;
  plannedEndAt?: string | null;
  actualStartAt?: string | null;
  actualEndAt?: string | null;

  plannedDistanceKm?: number | null;
  actualDistanceKm?: number | null;

  status: TransportStatus;
  pickupLocationId: number | null;
  deliveryLocationId: number | null;
}

// Paged transport response (useful for API list endpoints)
export type TransportPage = Page<TransportDTO>;

export interface CreateTransportRequest {
  vehicleId: number;
  driverId?: number | null;
  trailerId: number;
  pickupLocationId: number;
  deliveryLocationId: number;

  contractualDueAt?: string | null;   // ISO (Instant)
  plannedStartAt?: string | null;
  plannedEndAt?: string | null;

  plannedDistanceKm?: number | null;
}

export interface UpdateTransportStatusRequest {
  status: TransportStatus;
}

// DETAILS / MODAL / PAGE
export interface TransportDetailsDTO {
  id: number;

  vehicleId: number;
  trailerId: number;
  driverId?: number | null;

  createdById: number;
  createdByEmail?: string | null;

  contractualDueAt?: string | null;
  plannedStartAt?: string | null;
  plannedEndAt?: string | null;
  actualStartAt?: string | null;
  actualEndAt?: string | null;

  plannedDistanceKm?: number | null;
  actualDistanceKm?: number | null;

  status: TransportStatus;

  pickupLocation: LocationDTO;
  deliveryLocation: LocationDTO;
}



