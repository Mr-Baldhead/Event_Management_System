// Patrol model interface (Scout troop)
export interface Patrol {
  id?: number;
  name: string;
  description?: string;
  eventId?: number;
  eventName?: string;
  participantCount?: number;
}

// Patrol form data for create/update
export interface PatrolFormData {
  name: string;
  description?: string;
  eventId?: number;
}
