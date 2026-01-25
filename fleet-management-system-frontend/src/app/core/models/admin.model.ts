import { UserRole } from './user-role.model';
import { AccountStatus } from "./account-status.model";
import { UserResponseDTO } from "./user.model";
import { AccountResponseDTO } from "./account.model";


export interface AdminCreateUserWithAccountRequest {
  firstName: string;
  middleName?: string | null;
  lastName: string;
  email: string;
  phone?: string | null;
  birthDate?: string | null; // YYYY-MM-DD

  login: string;
  password: string;
  roles: UserRole[];
  status?: AccountStatus | null;
}

export interface AdminCreateUserWithAccountResponseDTO {
  user: UserResponseDTO;
  account: AccountResponseDTO;
}



