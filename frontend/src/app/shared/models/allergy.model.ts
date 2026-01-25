// Allergy model
export interface Allergy {
  id: number;
  eventId: number;
  name: string;
  sortOrder: number;
}

// Allergy report entry
export interface AllergyReportEntry {
  allergyName: string;
  participants: AllergyParticipant[];
  count: number;
}

// Participant in allergy report
export interface AllergyParticipant {
  id: number;
  fullName: string;
  patrol?: string;
  otherInfo?: string;
}

// Allergy report response
export interface AllergyReport {
  eventId: number;
  eventName: string;
  allergies: AllergyReportEntry[];
  totalParticipantsWithAllergies: number;
  generatedAt: string;
}
