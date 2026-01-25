// Allergen severity levels
export type AllergenSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

// Allergen model interface
export interface Allergen {
  id?: number;
  name: string;
  description?: string;
  severity: AllergenSeverity;
  participantCount?: number;
}

// Allergen form data for create/update
export interface AllergenFormData {
  name: string;
  description?: string;
  severity: AllergenSeverity;
}
