import { Injectable, signal, inject, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError, BehaviorSubject } from 'rxjs';

export interface User {
    id: number;
    email: string;
    firstName: string | null;
    lastName: string | null;
    role: 'SUPERADMIN' | 'ADMIN';
    mustChangePassword: boolean;
    locked: boolean;
    createdAt: string;
    lastLogin: string | null;
    initials: string;
    fullName: string;
}

export interface LoginResponse {
    token: string;
    user: User;
    mustChangePassword: boolean;
    message: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly apiUrl = '/api/auth';
    private http = inject(HttpClient);
    private router = inject(Router);

    // Current user state
    private currentUserSubject = new BehaviorSubject<User | null>(null);
    currentUser$ = this.currentUserSubject.asObservable();

    // Signal-based state
    private readonly _currentUser = signal<User | null>(null);
    private readonly _loading = signal(false);
    private readonly _isAuthenticated = signal(false);

    // Public readonly signals
    readonly currentUser = this._currentUser.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly isAuthenticated = this._isAuthenticated.asReadonly();

    // Computed signals for template use
    readonly isLoggedIn = computed(() => this._isAuthenticated());
    readonly userName = computed(() => {
        const user = this._currentUser();
        return user?.fullName || user?.email || '';
    });

    constructor() {
        // Check if user is already logged in on service init
        this.checkAuth();
    }

    // Login
    login(email: string, password: string): Observable<LoginResponse> {
        this._loading.set(true);
        return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password }, {
            withCredentials: true
        }).pipe(
            tap(response => {
                this._currentUser.set(response.user);
                this.currentUserSubject.next(response.user);
                this._isAuthenticated.set(true);
                this._loading.set(false);
            }),
            catchError(error => {
                this._loading.set(false);
                return throwError(() => error);
            })
        );
    }

    // Logout
    logout(): void {
        this.http.post(`${this.apiUrl}/logout`, {}, {
            withCredentials: true
        }).subscribe({
            next: () => {
                this.clearAuthState();
                this.router.navigate(['/login']);
            },
            error: () => {
                // Even if logout fails on server, clear local state
                this.clearAuthState();
                this.router.navigate(['/login']);
            }
        });
    }

    // Clear authentication state
    private clearAuthState(): void {
        this._currentUser.set(null);
        this.currentUserSubject.next(null);
        this._isAuthenticated.set(false);
    }

    // Check authentication status
    checkAuth(): void {
        this.http.get<User>(`${this.apiUrl}/me`, {
            withCredentials: true
        }).subscribe({
            next: (user) => {
                this._currentUser.set(user);
                this.currentUserSubject.next(user);
                this._isAuthenticated.set(true);
            },
            error: () => {
                this.clearAuthState();
            }
        });
    }

    // Change password
    changePassword(currentPassword: string | null, newPassword: string): Observable<any> {
        return this.http.post(`${this.apiUrl}/change-password`, {
            currentPassword,
            newPassword
        }, {
            withCredentials: true
        });
    }

    // Get current user synchronously
    getCurrentUser(): User | null {
        return this._currentUser();
    }

    // Get token - not used with cookie-based auth, but kept for compatibility
    getToken(): string | null {
        // With HttpOnly cookies, we don't have access to the token in JS
        // This method is kept for compatibility but returns null
        return null;
    }

    // Check if user has specific role
    hasRole(role: 'SUPERADMIN' | 'ADMIN'): boolean {
        const user = this._currentUser();
        return user?.role === role;
    }

    // Check if user is SuperAdmin
    isSuperAdmin(): boolean {
        return this.hasRole('SUPERADMIN');
    }

    // Check if user is Admin
    isAdmin(): boolean {
        return this.hasRole('ADMIN') || this.hasRole('SUPERADMIN');
    }
}