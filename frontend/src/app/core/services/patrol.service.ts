import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Patrol, PatrolFormData } from '../models/patrol.model';
import { environment } from '../../../environments/environment';

// Service for Patrol API operations
@Injectable({
  providedIn: 'root'
})
export class PatrolService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/patrols`;

  // Get all patrols
  getAll(): Observable<Patrol[]> {
    return this.http.get<Patrol[]>(this.baseUrl);
  }

  // Get patrol by ID
  getById(id: number): Observable<Patrol> {
    return this.http.get<Patrol>(`${this.baseUrl}/${id}`);
  }

  // Get patrols by event
  getByEvent(eventId: number): Observable<Patrol[]> {
    return this.http.get<Patrol[]>(`${this.baseUrl}/event/${eventId}`);
  }

  // Search patrols by name
  search(name: string): Observable<Patrol[]> {
    return this.http.get<Patrol[]>(`${this.baseUrl}/search`, {
      params: { name }
    });
  }

  // Create new patrol
  create(patrol: PatrolFormData): Observable<Patrol> {
    return this.http.post<Patrol>(this.baseUrl, patrol);
  }

  // Update patrol
  update(id: number, patrol: PatrolFormData): Observable<Patrol> {
    return this.http.put<Patrol>(`${this.baseUrl}/${id}`, patrol);
  }

  // Delete patrol
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
