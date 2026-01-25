import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Participant, ParticipantFormData } from '../models/participant.model';
import { environment } from '../../../environments/environment';

// Service for Participant API operations
@Injectable({
  providedIn: 'root'
})
export class ParticipantService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/participants`;

  // Get all participants
  getAll(): Observable<Participant[]> {
    return this.http.get<Participant[]>(this.baseUrl);
  }

  // Get participant by ID
  getById(id: number): Observable<Participant> {
    return this.http.get<Participant>(`${this.baseUrl}/${id}`);
  }

  // Get participants by patrol
  getByPatrol(patrolId: number): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.baseUrl}/patrol/${patrolId}`);
  }

  // Get participants with allergens
  getWithAllergens(): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.baseUrl}/with-allergens`);
  }

  // Get minor participants
  getMinors(): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.baseUrl}/minors`);
  }

  // Search participants by name
  search(name: string): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.baseUrl}/search`, {
      params: { name }
    });
  }

  // Create new participant
  create(participant: ParticipantFormData): Observable<Participant> {
    return this.http.post<Participant>(this.baseUrl, participant);
  }

  // Update participant
  update(id: number, participant: ParticipantFormData): Observable<Participant> {
    return this.http.put<Participant>(`${this.baseUrl}/${id}`, participant);
  }

  // Update participant allergens
  updateAllergens(id: number, allergenIds: number[]): Observable<Participant> {
    return this.http.put<Participant>(`${this.baseUrl}/${id}/allergens`, allergenIds);
  }

  // Delete participant
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
