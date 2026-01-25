import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Registration, RegistrationFormData } from '../models/registration.model';
import { environment } from '../../../environments/environment';

// Service for Registration API operations
@Injectable({
  providedIn: 'root'
})
export class RegistrationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/registrations`;

  // Get all registrations
  getAll(): Observable<Registration[]> {
    return this.http.get<Registration[]>(this.baseUrl);
  }

  // Get registration by ID
  getById(id: number): Observable<Registration> {
    return this.http.get<Registration>(`${this.baseUrl}/${id}`);
  }

  // Get registrations by event
  getByEvent(eventId: number): Observable<Registration[]> {
    return this.http.get<Registration[]>(`${this.baseUrl}/event/${eventId}`);
  }

  // Get registrations by participant
  getByParticipant(participantId: number): Observable<Registration[]> {
    return this.http.get<Registration[]>(`${this.baseUrl}/participant/${participantId}`);
  }

  // Create new registration
  create(registration: RegistrationFormData): Observable<Registration> {
    return this.http.post<Registration>(this.baseUrl, registration);
  }

  // Confirm registration
  confirm(id: number): Observable<Registration> {
    return this.http.put<Registration>(`${this.baseUrl}/${id}/confirm`, {});
  }

  // Cancel registration
  cancel(id: number): Observable<Registration> {
    return this.http.put<Registration>(`${this.baseUrl}/${id}/cancel`, {});
  }

  // Update registration notes
  updateNotes(id: number, notes: string): Observable<Registration> {
    return this.http.put<Registration>(`${this.baseUrl}/${id}/notes`, notes);
  }

  // Delete registration
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
