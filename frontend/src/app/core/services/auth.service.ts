import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of } from 'rxjs';

// User model
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'SUPERADMIN';
}

// Login request model
export interface LoginRequest {
  email: string;
  password: string;
}

// Login response model
export interface LoginResponse {
  token: string;
  user: User;
}

/**
 * Authentication service for managing user login state.
 * Uses Angular signals for reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  
  // Signals for reactive state
  private _currentUser = signal<User | null>(null);
  private _isLoading = signal<boolean>(false);
  private _error = signal<string | null>(null);
  
  // Public readonly signals
  readonly currentUser = this._currentUser.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  readonly error = this._error.asReadonly();
  
  // Alias for backwards compatibility
  readonly loading = this._isLoading.asReadonly();
  
  // Computed values
  readonly isLoggedIn = computed(() => this._currentUser() !== null);
  readonly userName = computed(() => {
    const user = this._currentUser();
    return user ? `${user.firstName} ${user.lastName}` : '';
  });

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.initializeFromStorage();
  }

  /**
   * Check if user is authenticated - REQUIRED by auth guards
   */
  isAuthenticated(): boolean {
    return this._currentUser() !== null;
  }

  /**
   * Check if user has admin role
   */
  isAdmin(): boolean {
    const role = this._currentUser()?.role;
    return role === 'ADMIN' || role === 'SUPERADMIN';
  }

  /**
   * Check if user has superadmin role
   */
  isSuperAdmin(): boolean {
    return this._currentUser()?.role === 'SUPERADMIN';
  }

  /**
   * Initialize user state from localStorage
   */
  private initializeFromStorage(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userJson = localStorage.getItem(this.USER_KEY);
    
    if (token && userJson) {
      try {
        const user = JSON.parse(userJson) as User;
        this._currentUser.set(user);
      } catch {
        this.clearStorage();
      }
    }
  }

  /**
   * Login with email and password
   */
  login(credentials: LoginRequest): Observable<LoginResponse | null> {
    this._isLoading.set(true);
    this._error.set(null);
    
    return this.http.post<LoginResponse>('/api/auth/login', credentials).pipe(
      tap(response => {
        this.storeAuthData(response.token, response.user);
        this._currentUser.set(response.user);
        this._isLoading.set(false);
        this._error.set(null);
      }),
      catchError(error => {
        console.error('Login failed:', error);
        this._isLoading.set(false);
        this._error.set(error.error?.message || 'Inloggningen misslyckades. Kontrollera dina uppgifter.');
        return of(null);
      })
    );
  }

  /**
   * Logout and clear all auth data
   */
  logout(): void {
    this.clearStorage();
    this._currentUser.set(null);
    this._error.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Get the stored auth token
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Check if user has a valid token
   */
  hasValidToken(): boolean {
    const token = this.getToken();
    return token !== null;
  }

  /**
   * Store authentication data in localStorage
   */
  private storeAuthData(token: string, user: User): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Clear all stored authentication data
   */
  private clearStorage(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  /**
   * Clear error message
   */
  clearError(): void {
    this._error.set(null);
  }

  /**
   * Update current user profile
   */
  updateProfile(user: User): void {
    this._currentUser.set(user);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  // =========================================================================
  // Development helpers - Remove in production!
  // =========================================================================
  
  /**
   * Dev login - bypasses backend for testing
   */
  devLogin(role: 'ADMIN' | 'SUPERADMIN' = 'ADMIN'): void {
    const mockUser: User = {
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'Användare',
      role: role
    };
    
    this.storeAuthData('dev-token-' + Date.now(), mockUser);
    this._currentUser.set(mockUser);
    this._error.set(null);
  }
}
