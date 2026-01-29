import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { User } from './auth.service';

export interface CreateUserRequest {
    firstName?: string;
    lastName?: string;
    email: string;
    password: string;
    sendNotification: boolean;
}

export interface UserCounts {
    total: number;
    admins: number;
    superadmins: number;
}

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly apiUrl = '/api/users';
    private http = inject(HttpClient);

    // State signals
    private readonly _users = signal<User[]>([]);
    private readonly _loading = signal(false);
    private readonly _error = signal<string | null>(null);

    readonly users = this._users.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    // Get all admin users
    getAdmins(): Observable<User[]> {
        this._loading.set(true);
        this._error.set(null);

        return this.http.get<User[]>(this.apiUrl, { withCredentials: true }).pipe(
            tap(users => {
                this._users.set(users);
                this._loading.set(false);
            }),
            catchError(error => {
                this._error.set('Kunde inte hämta användare');
                this._loading.set(false);
                return throwError(() => error);
            })
        );
    }

    // Get user counts
    getCounts(): Observable<UserCounts> {
        return this.http.get<UserCounts>(`${this.apiUrl}/counts`, { withCredentials: true });
    }

    // Create new admin
    createAdmin(request: CreateUserRequest): Observable<User> {
        return this.http.post<User>(this.apiUrl, request, { withCredentials: true }).pipe(
            tap(created => {
                this._users.update(users => [...users, created]);
            }),
            catchError(error => {
                this._error.set(error.error?.error || 'Kunde inte skapa användare');
                return throwError(() => error);
            })
        );
    }

    // Update user
    updateUser(id: number, user: Partial<User>): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}/${id}`, user, { withCredentials: true }).pipe(
            tap(updated => {
                this._users.update(users => users.map(u => u.id === id ? updated : u));
            }),
            catchError(error => {
                this._error.set(error.error?.error || 'Kunde inte uppdatera användare');
                return throwError(() => error);
            })
        );
    }

    // Delete user
    deleteUser(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true }).pipe(
            tap(() => {
                this._users.update(users => users.filter(u => u.id !== id));
            }),
            catchError(error => {
                this._error.set(error.error?.error || 'Kunde inte ta bort användare');
                return throwError(() => error);
            })
        );
    }

    // Lock/unlock user
    setUserLocked(id: number, locked: boolean): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}/${id}/lock?locked=${locked}`, {}, { withCredentials: true }).pipe(
            tap(updated => {
                this._users.update(users => users.map(u => u.id === id ? updated : u));
            }),
            catchError(error => {
                this._error.set(error.error?.error || 'Kunde inte uppdatera användare');
                return throwError(() => error);
            })
        );
    }

    // Reset user password
    resetPassword(id: number): Observable<{ message: string; newPassword: string }> {
        return this.http.post<{ message: string; newPassword: string }>(
            `${this.apiUrl}/${id}/reset-password`,
            {},
            { withCredentials: true }
        );
    }

    // Clear error
    clearError(): void {
        this._error.set(null);
    }

    // Generate random password (client-side)
    generatePassword(length: number = 16): string {
        const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        const lowercase = 'abcdefghijklmnopqrstuvwxyz';
        const digits = '0123456789';
        const special = '!@#$%^&*()_+-=';
        const allChars = uppercase + lowercase + digits + special;

        let password = '';

        // Ensure at least one of each type
        password += uppercase[Math.floor(Math.random() * uppercase.length)];
        password += lowercase[Math.floor(Math.random() * lowercase.length)];
        password += digits[Math.floor(Math.random() * digits.length)];
        password += special[Math.floor(Math.random() * special.length)];

        // Fill the rest
        for (let i = 4; i < length; i++) {
            password += allChars[Math.floor(Math.random() * allChars.length)];
        }

        // Shuffle
        return password.split('').sort(() => Math.random() - 0.5).join('');
    }

    // Calculate password strength
    calculatePasswordStrength(password: string): 'svagt' | 'mellan' | 'starkt' | null {
        if (!password) return null;

        let strength = 0;

        if (password.length >= 8) strength++;
        if (password.length >= 12) strength++;
        if (/[a-z]/.test(password)) strength++;
        if (/[A-Z]/.test(password)) strength++;
        if (/[0-9]/.test(password)) strength++;
        if (/[^a-zA-Z0-9]/.test(password)) strength++;

        if (strength <= 2) return 'svagt';
        if (strength <= 4) return 'mellan';
        return 'starkt';
    }
}
