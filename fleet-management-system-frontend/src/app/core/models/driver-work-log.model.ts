import { ActivityType } from './activity-type.model';

export interface DriverWorkLogDTO {
  id: number;
  driverId: number;
  driverName?: string | null;
  transportId?: number;
  activityType: ActivityType;
  startTime: string;              // ISO
  endTime?: string | null;        // ISO
  breakDuration?: number | null;
  notes?: string | null;
}

export interface CreateDriverWorkLogRequest {
  startTime: string;             // ISO
  endTime?: string | null;       // ISO
  breakDuration?: number | null;
  notes?: string | null;
  driverId: number;
  transportId?: number;
  activityType: ActivityType;
}

export interface UpdateDriverWorkLogRequest {
  startTime?: string | null;
  endTime?: string | null;
  breakDuration?: number | null;
  notes?: string | null;
  driverId?: number | null;
  transportId?: number | null;
  activityType?: ActivityType | null;
}



