export interface RegisterRequest {
  nombre: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export type GameHarborRole = 'CLIENTE' | 'ADMINISTRADOR';

export interface AuthUser {
  id: number;
  nombre: string;
  email: string;
  saldo: number;
  roles: GameHarborRole[];
}

export interface AuthResponse {
  token: string;
  usuario: AuthUser;
}

export class AuthApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly fieldErrors: Readonly<Record<string, string>> = {},
  ) {
    super(message);
    this.name = 'AuthApiError';
  }
}
