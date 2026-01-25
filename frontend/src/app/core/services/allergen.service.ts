import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Allergen, AllergenFormData, AllergenSeverity } from '../models/allergen.model';
import { environment } from '../../../environments/environment';

// Service for Allergen API operations
@Injectable({
  providedIn: 'root'
})
export class AllergenService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/allergens`;

  // Get all allergens
  getAll(): Observable<Allergen[]> {
    return this.http.get<Allergen[]>(this.baseUrl);
  }

  // Get allergen by ID
  getById(id: number): Observable<Allergen> {
    return this.http.get<Allergen>(`${this.baseUrl}/${id}`);
  }

  // Get critical allergens (HIGH and CRITICAL severity)
  getCritical(): Observable<Allergen[]> {
    return this.http.get<Allergen[]>(`${this.baseUrl}/critical`);
  }

  // Get allergens by severity
  getBySeverity(severity: AllergenSeverity): Observable<Allergen[]> {
    return this.http.get<Allergen[]>(`${this.baseUrl}/severity/${severity}`);
  }

  // Search allergens by name
  search(name: string): Observable<Allergen[]> {
    return this.http.get<Allergen[]>(`${this.baseUrl}/search`, {
      params: { name }
    });
  }

  // Create new allergen
  create(allergen: AllergenFormData): Observable<Allergen> {
    return this.http.post<Allergen>(this.baseUrl, allergen);
  }

  // Update allergen
  update(id: number, allergen: AllergenFormData): Observable<Allergen> {
    return this.http.put<Allergen>(`${this.baseUrl}/${id}`, allergen);
  }

  // Delete allergen
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
