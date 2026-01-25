import { UserRole } from "./user-role.model";
import { AccountStatus } from "./account-status.model";
import { UserResponseDTO } from "./user.model";

/** Single role */
export interface AccountRoleDTO {
  role: UserRole;
}

export interface AccountUserShortDTO {
  accountId: number;
  login: string;
  status: AccountStatus;          // backend String enum
  createdAt: string;
  lastLoginAt: string | null;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
}

/**  Backend: AccountResponseDTO (Java record) */
export interface AccountResponseDTO {
  id: number;
  login: string;
  status: AccountStatus;
  createdAt: string;          // Instant -> ISO string
  lastLoginAt: string | null; // Instant -> ISO string, can be null
  userId: number;
  roles: UserRole[];          // Set<UserRole> -> UserRole[]
}

/** DTO for account status change */
export interface AccountStatusUpdateDTO {
  status: AccountStatus;
}

/** DTO for password change */
export interface AccountChangePasswordDTO {
  oldPassword: string;
  newPassword: string;
}

/** Account + User VIEW  */
export interface AccountUserViewDTO {
  account: AccountResponseDTO;
  user: UserResponseDTO;
}


export interface AccountRegisterDTO {
  login: string;
  password: string;
  userId?: number;
  roles?: UserRole[];
  status?: AccountStatus;
}

export interface AccountAdminResetPasswordDTO  {
  newPassword: string;
}

export interface AccountRolesUpdateDTO {
  roles: UserRole[];
}
/** LOGIN */
export interface AccountLoginDTO {
  login: string;
  password: string;
}




