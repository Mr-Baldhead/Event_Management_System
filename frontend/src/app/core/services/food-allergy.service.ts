import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { FoodAllergy } from '../../shared/models/form-field.model';

// Service for managing global food allergies
@Injectable({
    providedIn: 'root'
})
export class FoodAllergyService {
    private readonly apiUrl = '/api/food-allergies';

    // Signals for reactive state
    private readonly _allergies = signal<FoodAllergy[]>([]);
    private readonly _loading = signal<boolean>(false);
    private readonly _error = signal<string | null>(null);

    // Public readonly signals
    readonly allergies = this._allergies.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    constructor(private http: HttpClient) {}

    // Load all food allergies
    loadAllergies(): Observable<FoodAllergy[]> {
        this._loading.set(true);
        this._error.set(null);

        return this.http.get<FoodAllergy[]>(this.apiUrl).pipe(
            tap(allergies => {
                this._allergies.set(allergies);
                this._loading.set(false);
            }),
            catchError(error => {
                this._error.set('Kunde inte ladda matallergier');
                this._loading.set(false);
                return throwError(() => error);
            })
        );
    }

    // Get all allergies (returns current value)
    getAllergies(): FoodAllergy[] {
        return this._allergies();
    }

    // Create new food allergy
    createAllergy(name: string): Observable<FoodAllergy> {
        return this.http.post<FoodAllergy>(this.apiUrl, { name }).pipe(
            tap(created => {
                this._allergies.update(allergies => [...allergies, created]);
            }),
            catchError(error => {
                this._error.set('Kunde inte skapa matallergi');
                return throwError(() => error);
            })
        );
    }

    // Update food allergy
    updateAllergy(id: number, name: string): Observable<FoodAllergy> {
        return this.http.put<FoodAllergy>(`${this.apiUrl}/${id}`, { name }).pipe(
            tap(updated => {
                this._allergies.update(allergies =>
                    allergies.map(a => a.id === id ? updated : a)
                );
            }),
            catchError(error => {
                this._error.set('Kunde inte uppdatera matallergi');
                return throwError(() => error);
            })
        );
    }

    // Delete food allergy
    deleteAllergy(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
            tap(() => {
                this._allergies.update(allergies => allergies.filter(a => a.id !== id));
            }),
            catchError(error => {
                this._error.set('Kunde inte ta bort matallergi');
                return throwError(() => error);
            })
        );
    }

    // Clear error
    clearError(): void {
        this._error.set(null);
    }
}
