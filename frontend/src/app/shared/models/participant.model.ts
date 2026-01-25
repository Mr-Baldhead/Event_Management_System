// Participant model
export interface Participant {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  birthDate?: string;
  personalNumber?: string;
  streetAddress?: string;
  postalCode?: string;
  city?: string;
  hasAllergies: boolean;
  registrationDate?: string;
  troopId?: number;
  troopName?: string;
  guardian?: Guardian;
  allergies?: ParticipantAllergy[];
}

// Guardian model - for minors
export interface Guardian {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

// Participant allergy
export interface ParticipantAllergy {
  allergyId: number;
  allergyName: string;
  otherInfo?: string;
}

// Registration model
export interface Registration {
  id: number;
  eventId: number;
  participantId: number;
  participant: Participant;
  registrationDate: string;
  status: RegistrationStatus;
}

export enum RegistrationStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED',
  WAITLISTED = 'WAITLISTED'
}

// Helper to get full name
export function getFullName(participant: Participant): string {
  return `${participant.lastName} ${participant.firstName}`;
}

// Check if participant is minor (under 18)
export function isMinor(birthDate: string): boolean {
  const birth = new Date(birthDate);
  const today = new Date();
  const age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    return age - 1 < 18;
  }
  return age < 18;
}
