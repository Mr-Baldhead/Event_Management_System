import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { FormField } from '../../shared/models/form-field.model';

// Service for managing form fields
@Injectable({
    providedIn: 'root'
})
export class FormFieldService {
    private readonly apiUrl = '/api/events';

    // Signals for reactive state
    private readonly _fields = signal<FormField[]>([]);
    private readonly _loading = signal<boolean>(false);
    private readonly _saving = signal<boolean>(false);
    private readonly _error = signal<string | null>(null);

    // Public readonly signals
    readonly fields = this._fields.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly saving = this._saving.asReadonly();
    readonly error = this._error.asReadonly();

    constructor(private http: HttpClient) {}

    // Get all fields for an event
    getFields(eventId: number): Observable<FormField[]> {
        this._loading.set(true);
        this._error.set(null);

        return this.http.get<FormField[]>(`${this.apiUrl}/${eventId}/form/fields`).pipe(
            tap(fields => {
                this._fields.set(fields);
                this._loading.set(false);
            }),
            catchError(error => {
                this._error.set('Kunde inte ladda formulärfält');
                this._loading.set(false);
                return throwError(() => error);
            })
        );
    }

    // Create a new field
    createField(eventId: number, field: FormField): Observable<FormField> {
        return this.http.post<FormField>(`${this.apiUrl}/${eventId}/form/fields`, field).pipe(
            tap(created => {
                this._fields.update(fields => [...fields, created]);
            }),
            catchError(error => {
                this._error.set('Kunde inte skapa fält');
                return throwError(() => error);
            })
        );
    }

    // Update a field
    updateField(eventId: number, fieldId: number, field: FormField): Observable<FormField> {
        return this.http.put<FormField>(`${this.apiUrl}/${eventId}/form/fields/${fieldId}`, field).pipe(
            tap(updated => {
                this._fields.update(fields =>
                    fields.map(f => f.id === fieldId ? updated : f)
                );
            }),
            catchError(error => {
                this._error.set('Kunde inte uppdatera fält');
                return throwError(() => error);
            })
        );
    }

    // Delete a field
    deleteField(eventId: number, fieldId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${eventId}/form/fields/${fieldId}`).pipe(
            tap(() => {
                this._fields.update(fields => fields.filter(f => f.id !== fieldId));
            }),
            catchError(error => {
                this._error.set('Kunde inte ta bort fält');
                return throwError(() => error);
            })
        );
    }

    // Save all fields (bulk update)
    saveAllFields(eventId: number, fields: FormField[]): Observable<FormField[]> {
        this._saving.set(true);
        this._error.set(null);

        return this.http.put<FormField[]>(`${this.apiUrl}/${eventId}/form/fields`, fields).pipe(
            tap(saved => {
                this._fields.set(saved);
                this._saving.set(false);
            }),
            catchError(error => {
                this._error.set('Kunde inte spara formuläret');
                this._saving.set(false);
                return throwError(() => error);
            })
        );
    }

    // Reorder fields
    reorderFields(eventId: number, fieldIds: number[]): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${eventId}/form/fields/reorder`, fieldIds).pipe(
            catchError(error => {
                this._error.set('Kunde inte ändra ordning');
                return throwError(() => error);
            })
        );
    }

    // Clear state
    clearFields(): void {
        this._fields.set([]);
        this._error.set(null);
    }

    // Clear error
    clearError(): void {
        this._error.set(null);
    }
}