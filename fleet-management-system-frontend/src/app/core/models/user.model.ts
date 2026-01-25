
export interface UserResponseDTO {
  id: number;
  firstName: string;
  middleName?: string | null;
  lastName: string;
  email: string;
  phone?: string | null;
  birthDate?: string | null; // ISO date
  accountId?: number | null;
}

export interface UserCreateDTO {
  firstName: string;
  middleName?: string | null;
  lastName: string;
  email: string;
  phone?: string | null;
  birthDate?: string | null; // "YYYY-MM-DD"
}

export interface UserUpdateDTO {
  firstName?: string | null;
  middleName?: string | null;
  lastName?: string | null;
  email?: string | null;
  phone?: string | null;
  birthDate?: string | null;
}



