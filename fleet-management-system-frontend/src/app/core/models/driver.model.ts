import { DriverStatus } from "./driver-status.model";

export interface DriverDTO {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string | null;
  driverLicenseNumber?: string | null;
  driverLicenseCategory?: string | null;
  driverLicenseExpiryDate?: string | null;
  driverStatus: DriverStatus;
  hasTransports: boolean;
  hasWorkLogs: boolean;
}

export interface CreateDriverRequest {
  userId: number;
  driverLicenseNumber?: string | null;
  driverLicenseCategory?: string | null;
  driverLicenseExpiryDate?: string | null; // 'YYYY-MM-DD'
}

export interface UpdateDriverRequest {
  driverLicenseNumber?: string | null;
  driverLicenseCategory?: string | null;
  driverLicenseExpiryDate?: string | null;
}





