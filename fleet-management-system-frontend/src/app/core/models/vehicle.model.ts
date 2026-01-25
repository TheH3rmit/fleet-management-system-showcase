import { VehicleStatus } from "./vehicle-status.model";

export interface VehicleDTO {
  id: number;
  manufacturer: string;
  model: string;
  dateOfProduction: string;
  mileage: number;
  fuelType: string;
  vehicleStatus: VehicleStatus;
  licensePlate: string;
  allowedLoad: number;
  insuranceNumber: string;
  assignedToTransport?: boolean;
  inProgressAssigned?: boolean;
}

export interface CreateVehicleRequest {
  manufacturer: string;
  model: string;
  dateOfProduction?: string | null; // 'YYYY-MM-DD'
  mileage?: number | null;
  fuelType?: string | null;
  licensePlate: string;
  allowedLoad?: number | null;
  insuranceNumber?: string | null;
}



