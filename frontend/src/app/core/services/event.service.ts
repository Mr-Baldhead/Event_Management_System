import { Injectable, signal, computed } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { Event, EventCreateDTO } from '../../shared/models/event.model';

// Partial update DTO for PATCH operations
export interface EventPatchDTO {
    active?: boolean;
    name?: string;
    description?: string;
}

// Service for managing events with Angular signals
@Injectable({
    providedIn: 'root'
})
export class EventService {
    private readonly apiUrl = '/api/events';

    // Signals for reactive state management
    private readonly _events = signal<Event[]>([]);
    private readonly _selectedEvent = signal<Event | null>(null);
    private readonly _loading = signal<boolean>(false);
    private readonly _error = signal<string | null>(null);

    // Public readonly signals
    readonly events = this._events.asReadonly();
    readonly selectedEvent = this._selectedEvent.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    // Computed signals
    readonly activeEvents = computed(() =>
        this._events().filter(e => e.active)
    );

    readonly eventCount = computed(() => this._events().length);

    readonly hasEvents = computed(() => this._events().length > 0);

    constructor(private http: HttpClient) {}

    // Load all events
    loadEvents(): void {
        this._loading.set(true);
        this._error.set(null);

        this.http.get<Event[]>(this.apiUrl)
            .pipe(
                tap(events => {
                    this._events.set(events);
                    this._loading.set(false);
                }),
                catchError(error => this.handleError(error))
            )
            .subscribe();
    }

    // Get all events as observable (for one-time use)
    getEvents(): Observable<Event[]> {
        return this.http.get<Event[]>(this.apiUrl);
    }

    // Load single event by ID
    loadEvent(id: number): void {
        this._loading.set(true);
        this._error.set(null);

        this.http.get<Event>(`${this.apiUrl}/${id}`)
            .pipe(
                tap(event => {
                    this._selectedEvent.set(event);
                    this._loading.set(false);
                }),
                catchError(error => this.handleError(error))
            )
            .subscribe();
    }

    // Get single event by ID as observable
    getEvent(id: number): Observable<Event> {
        return this.http.get<Event>(`${this.apiUrl}/${id}`);
    }

    // Get event by slug
    getEventBySlug(slug: string): Observable<Event> {
        return this.http.get<Event>(`${this.apiUrl}/slug/${slug}`);
    }

    // Create new event
    createEvent(event: EventCreateDTO): Observable<Event> {
        this._loading.set(true);
        this._error.set(null);

        return this.http.post<Event>(this.apiUrl, event)
            .pipe(
                tap(newEvent => {
                    this._events.update(events => [...events, newEvent]);
                    this._loading.set(false);
                }),
                catchError(error => this.handleError(error))
            );
    }

    // Update existing event
    updateEvent(id: number, event: EventCreateDTO): Observable<Event> {
        this._loading.set(true);
        this._error.set(null);

        return this.http.put<Event>(`${this.apiUrl}/${id}`, event)
            .pipe(
                tap(updatedEvent => {
                    this._events.update(events =>
                        events.map(e => e.id === id ? updatedEvent : e)
                    );
                    this._selectedEvent.set(updatedEvent);
                    this._loading.set(false);
                }),
                catchError(error => this.handleError(error))
            );
    }

    // Partial update (PATCH) - for toggling active status etc.
    patchEvent(id: number, patch: EventPatchDTO): Observable<Event> {
        return this.http.patch<Event>(`${this.apiUrl}/${id}`, patch)
            .pipe(
                tap(updatedEvent => {
                    this._events.update(events =>
                        events.map(e => e.id === id ? updatedEvent : e)
                    );
                    if (this._selectedEvent()?.id === id) {
                        this._selectedEvent.set(updatedEvent);
                    }
                }),
                catchError(error => this.handleError(error))
            );
    }

    // Delete event
    deleteEvent(id: number): Observable<void> {
        this._loading.set(true);
        this._error.set(null);

        return this.http.delete<void>(`${this.apiUrl}/${id}`)
            .pipe(
                tap(() => {
                    this._events.update(events => events.filter(e => e.id !== id));
                    if (this._selectedEvent()?.id === id) {
                        this._selectedEvent.set(null);
                    }
                    this._loading.set(false);
                }),
                catchError(error => this.handleError(error))
            );
    }

    // Clear selected event
    clearSelectedEvent(): void {
        this._selectedEvent.set(null);
    }

    // Clear error
    clearError(): void {
        this._error.set(null);
    }

    // Error handler
    private handleError(error: HttpErrorResponse): Observable<never> {
        let errorMessage = 'Ett oväntat fel inträffade';

        if (error.error instanceof ErrorEvent) {
            // Client-side error
            errorMessage = error.error.message;
        } else {
            // Server-side error
            if (error.status === 404) {
                errorMessage = 'Event hittades inte';
            } else if (error.status === 400) {
                errorMessage = error.error?.message || 'Ogiltig förfrågan';
            } else if (error.status === 500) {
                errorMessage = 'Serverfel - försök igen senare';
            }
        }

        this._error.set(errorMessage);
        this._loading.set(false);
        return throwError(() => new Error(errorMessage));
    }
}