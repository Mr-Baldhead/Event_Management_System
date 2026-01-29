import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { Troop } from '../../shared/models/form-field.model';

// Service for managing global troops
@Injectable({
    providedIn: 'root'
})
export class TroopService {
    private readonly apiUrl = '/api/troops';

    // Signals for reactive state
    private readonly _troops = signal<Troop[]>([]);
    private readonly _loading = signal<boolean>(false);
    private readonly _error = signal<string | null>(null);

    // Public readonly signals
    readonly troops = this._troops.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    constructor(private http: HttpClient) {}

    // Load all troops
    loadTroops(): Observable<Troop[]> {
        this._loading.set(true);
        this._error.set(null);

        return this.http.get<Troop[]>(this.apiUrl).pipe(
            tap(troops => {
                this._troops.set(troops);
                this._loading.set(false);
            }),
            catchError(error => {
                this._error.set('Kunde inte ladda kårer');
                this._loading.set(false);
                return throwError(() => error);
            })
        );
    }

    // Get all troops (returns current value)
    getTroops(): Troop[] {
        return this._troops();
    }

    // Create new troop
    createTroop(name: string): Observable<Troop> {
        return this.http.post<Troop>(this.apiUrl, { name }).pipe(
            tap(created => {
                this._troops.update(troops => [...troops, created]);
            }),
            catchError(error => {
                this._error.set('Kunde inte skapa kår');
                return throwError(() => error);
            })
        );
    }

    // Update troop
    updateTroop(id: number, name: string): Observable<Troop> {
        return this.http.put<Troop>(`${this.apiUrl}/${id}`, { name }).pipe(
            tap(updated => {
                this._troops.update(troops =>
                    troops.map(t => t.id === id ? updated : t)
                );
            }),
            catchError(error => {
                this._error.set('Kunde inte uppdatera kår');
                return throwError(() => error);
            })
        );
    }

    // Delete troop
    deleteTroop(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
            tap(() => {
                this._troops.update(troops => troops.filter(t => t.id !== id));
            }),
            catchError(error => {
                this._error.set('Kunde inte ta bort kår');
                return throwError(() => error);
            })
        );
    }

    // Clear error
    clearError(): void {
        this._error.set(null);
    }
}
