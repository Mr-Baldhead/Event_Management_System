// Event model interface
export interface Event {
  id?: number;
  name: string;
  slug?: string;
  description?: string;
  startDate: string;
  endDate: string;
  streetAddress?: string;
  city?: string;
  capacity?: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
  registrationCount?: number;
  confirmedCount?: number;
}

// Event form data for create/update
export interface EventFormData {
  name: string;
  description?: string;
  startDate: string;
  endDate: string;
  streetAddress?: string;
  city?: string;
  capacity?: number;
  active: boolean;
}
