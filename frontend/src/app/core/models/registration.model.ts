// Registration status types
export type RegistrationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'WAITLISTED';

// Registration model interface
export interface Registration {
  id?: number;
  eventId: number;
  eventName?: string;
  participantId: number;
  participantName?: string;
  status: RegistrationStatus;
  registrationDate?: string;
  confirmedAt?: string;
  cancelledAt?: string;
  notes?: string;
}

// Registration form data for create
export interface RegistrationFormData {
  eventId: number;
  participantId: number;
  notes?: string;
}
