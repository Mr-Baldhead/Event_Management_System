import { Allergen } from './allergen.model';

// Participant model interface
export interface Participant {
  id?: number;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  birthDate?: string;
  personalNumber?: string;
  streetAddress?: string;
  postalCode?: string;
  city?: string;
  patrolId?: number;
  patrolName?: string;
  allergens?: Allergen[];
  allergenIds?: number[];
  guardianName?: string;
  guardianPhone?: string;
  guardianEmail?: string;
  createdAt?: string;
  age?: number;
  minor?: boolean;
}

// Participant form data for create/update
export interface ParticipantFormData {
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  birthDate?: string;
  personalNumber?: string;
  streetAddress?: string;
  postalCode?: string;
  city?: string;
  patrolId?: number;
  allergenIds?: number[];
  guardianName?: string;
  guardianPhone?: string;
  guardianEmail?: string;
}
