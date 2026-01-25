// User model
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  locked: boolean;
  createdAt?: string;
  lastLogin?: string;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  SUPERADMIN = 'SUPERADMIN'
}

// Login credentials
export interface LoginCredentials {
  email: string;
  password: string;
}

// Login response
export interface LoginResponse {
  token: string;
  user: User;
  expiresIn: number;
}

// Helper to check if user has role
export function hasRole(user: User | null, ...roles: UserRole[]): boolean {
  if (!user) return false;
  return roles.includes(user.role);
}

// Helper to check if user is admin
export function isAdmin(user: User | null): boolean {
  return hasRole(user, UserRole.ADMIN, UserRole.SUPERADMIN);
}

// Helper to check if user is superadmin
export function isSuperAdmin(user: User | null): boolean {
  return hasRole(user, UserRole.SUPERADMIN);
}
