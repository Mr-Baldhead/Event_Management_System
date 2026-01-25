// Event model - represents an event in the system
export interface Event {
  id: number;
  name: string;
  slug: string;
  description?: string;
  startDate: string | number[];
  endDate: string | number[];
  streetAddress?: string;
  postalCode?: string;
  city?: string;
  capacity?: number;
  active: boolean;
  createdAt?: string | number[];
  updatedAt?: string | number[];
  registrationCount: number;
  remainingSpots?: number;
}

// DTO for creating/updating events
export interface EventCreateDTO {
  name: string;
  slug?: string;
  description?: string;
  startDate: string;
  endDate: string;
  streetAddress?: string;
  postalCode?: string;
  city?: string;
  capacity?: number;
  active?: boolean;
}

// Helper function to parse date array from backend
export function parseDateArray(dateArray: string | number[]): Date {
  if (typeof dateArray === 'string') {
    return new Date(dateArray);
  }
  // Format: [year, month, day, hour, minute] or [year, month, day, hour, minute, second]
  const [year, month, day, hour = 0, minute = 0, second = 0] = dateArray;
  return new Date(year, month - 1, day, hour, minute, second);
}

// Helper function to format date for display
export function formatDate(dateArray: string | number[]): string {
  const date = parseDateArray(dateArray);
  return date.toLocaleDateString('sv-SE');
}

// Helper function to format datetime for display
export function formatDateTime(dateArray: string | number[]): string {
  const date = parseDateArray(dateArray);
  return date.toLocaleString('sv-SE');
}

// Helper function to format date for input fields
export function formatDateForInput(dateArray: string | number[]): string {
  const date = parseDateArray(dateArray);
  return date.toISOString().slice(0, 10);
}

// Helper function to format datetime-local for input fields
export function formatDateTimeForInput(dateArray: string | number[]): string {
  const date = parseDateArray(dateArray);
  return date.toISOString().slice(0, 16);
}
