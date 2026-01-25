import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EventService } from '../../../core/services/event.service';
import { HeaderComponent } from '../../../shared/components/header/header.component';

/**
 * Event form component with Material datepickers and separate time inputs.
 * Handles both create and edit modes.
 */
@Component({
    selector: 'app-event-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterLink,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatSnackBarModule,
        MatCheckboxModule,
        MatProgressSpinnerModule,
        HeaderComponent
    ],
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'sv-SE' }
    ],
    template: `
        <app-header />

        <main class="container">
            <a routerLink="/events" class="back-link">
                <mat-icon>arrow_back</mat-icon>
                Tillbaka till alla events
            </a>

            <mat-card class="form-card">
                <mat-card-header>
                    <mat-card-title>
                        <mat-icon>{{ isEditMode() ? 'edit' : 'add_circle' }}</mat-icon>
                        {{ isEditMode() ? 'Redigera event' : 'Skapa nytt event' }}
                    </mat-card-title>
                </mat-card-header>

                <mat-card-content>
                    <div *ngIf="loading()" class="loading-container">
                        <mat-spinner diameter="40"></mat-spinner>
                        <span>Laddar...</span>
                    </div>

                    <form *ngIf="!loading()" [formGroup]="eventForm" (ngSubmit)="onSubmit()">

                        <!-- Event name -->
                        <mat-form-field appearance="outline" class="full-width">
                            <mat-label>Eventnamn</mat-label>
                            <input matInput formControlName="name" placeholder="T.ex. Sommarläger 2026">
                            <mat-icon matPrefix>event</mat-icon>
                            <mat-error>Eventnamn krävs</mat-error>
                        </mat-form-field>

                        <!-- Description -->
                        <mat-form-field appearance="outline" class="full-width">
                            <mat-label>Beskrivning</mat-label>
                            <textarea matInput formControlName="description" rows="2"
                                      placeholder="Beskriv eventet..."></textarea>
                            <mat-icon matPrefix>description</mat-icon>
                        </mat-form-field>

                        <!-- Date and time section -->
                        <h3 class="section-title">
                            <mat-icon>calendar_today</mat-icon>
                            Datum och tid
                        </h3>

                        <div class="date-time-row">
                            <mat-form-field appearance="outline" class="date-field">
                                <mat-label>Startdatum</mat-label>
                                <input matInput [matDatepicker]="startDatePicker" formControlName="startDate" placeholder="Välj datum">
                                <mat-datepicker-toggle matIconSuffix [for]="startDatePicker"></mat-datepicker-toggle>
                                <mat-datepicker #startDatePicker></mat-datepicker>
                                <mat-error>Startdatum krävs</mat-error>
                            </mat-form-field>

                            <mat-form-field appearance="outline" class="time-field">
                                <mat-label>Starttid</mat-label>
                                <input matInput type="time" formControlName="startTime">
                                <mat-icon matPrefix>schedule</mat-icon>
                            </mat-form-field>
                        </div>

                        <div class="date-time-row">
                            <mat-form-field appearance="outline" class="date-field">
                                <mat-label>Slutdatum</mat-label>
                                <input matInput [matDatepicker]="endDatePicker" formControlName="endDate" placeholder="Välj datum">
                                <mat-datepicker-toggle matIconSuffix [for]="endDatePicker"></mat-datepicker-toggle>
                                <mat-datepicker #endDatePicker></mat-datepicker>
                                <mat-error>Slutdatum krävs</mat-error>
                            </mat-form-field>

                            <mat-form-field appearance="outline" class="time-field">
                                <mat-label>Sluttid</mat-label>
                                <input matInput type="time" formControlName="endTime">
                                <mat-icon matPrefix>schedule</mat-icon>
                            </mat-form-field>
                        </div>

                        <!-- Date validation error -->
                        <div *ngIf="dateError()" class="validation-error">
                            <mat-icon>warning</mat-icon>
                            {{ dateError() }}
                        </div>

                        <!-- Location section -->
                        <h3 class="section-title">
                            <mat-icon>location_on</mat-icon>
                            Plats
                        </h3>

                        <mat-form-field appearance="outline" class="full-width">
                            <mat-label>Gatuadress</mat-label>
                            <input matInput formControlName="streetAddress" placeholder="T.ex. Scoutgården 1">
                            <mat-icon matPrefix>home</mat-icon>
                        </mat-form-field>

                        <div class="address-row">
                            <mat-form-field appearance="outline" class="postal-code-field">
                                <mat-label>Postnummer</mat-label>
                                <input matInput formControlName="postalCode" placeholder="12345">
                            </mat-form-field>

                            <mat-form-field appearance="outline" class="city-field">
                                <mat-label>Ort</mat-label>
                                <input matInput formControlName="city" placeholder="Stockholm">
                            </mat-form-field>
                        </div>

                        <!-- Capacity -->
                        <mat-form-field appearance="outline" class="full-width">
                            <mat-label>Max antal deltagare</mat-label>
                            <input matInput type="number" formControlName="capacity" min="0">
                            <mat-icon matPrefix>groups</mat-icon>
                            <mat-hint>Lämna tomt för obegränsat antal</mat-hint>
                        </mat-form-field>

                        <!-- Active checkbox -->
                        <div class="checkbox-row">
                            <mat-checkbox formControlName="active" color="primary">
                                Aktivera event (gör det synligt för anmälningar)
                            </mat-checkbox>
                        </div>

                        <!-- Submit buttons -->
                        <div class="form-actions">
                            <button mat-stroked-button type="button" routerLink="/events">
                                <mat-icon>close</mat-icon>
                                Avbryt
                            </button>
                            <button mat-flat-button color="primary" type="submit"
                                    [disabled]="eventForm.invalid || submitting() || !!dateError()">
                                <mat-spinner *ngIf="submitting()" diameter="20"></mat-spinner>
                                <mat-icon *ngIf="!submitting()">{{ isEditMode() ? 'save' : 'add' }}</mat-icon>
                                <span>{{ isEditMode() ? 'Spara ändringar' : 'Skapa event' }}</span>
                            </button>
                        </div>
                    </form>
                </mat-card-content>
            </mat-card>
        </main>
    `,
    styles: [`
      main {
        max-width: 700px;
        margin: 0 auto;
        padding: 24px 16px;
      }

      .back-link {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: #1D968C;
        text-decoration: none;
        font-weight: 500;
        margin-bottom: 16px;
        font-size: 14px;
      }

      .back-link:hover {
        text-decoration: underline;
      }

      .form-card mat-card-header {
        margin-bottom: 24px;
      }

      .form-card mat-card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 22px;
      }

      .form-card mat-card-title mat-icon {
        color: #1D968C;
      }

      .loading-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 48px;
        gap: 16px;
        color: #5F6166;
      }

      /* Compact form fields - 30% smaller height */
      :host ::ng-deep .mat-mdc-form-field {
        margin-bottom: 20px;
      }

      :host ::ng-deep .mat-mdc-form-field-subscript-wrapper {
        display: none;
      }

      :host ::ng-deep .mat-mdc-text-field-wrapper {
        padding-top: 0 !important;
        padding-bottom: 0 !important;
      }

      :host ::ng-deep .mat-mdc-form-field-infix {
        padding-top: 6px !important;
        padding-bottom: 6px !important;
        min-height: 32px !important;
      }

      :host ::ng-deep .mdc-text-field--outlined {
        --mdc-outlined-text-field-container-shape: 4px;
      }

      :host ::ng-deep .mat-mdc-form-field-flex {
        height: 40px !important;
        align-items: center;
      }

      :host ::ng-deep .mat-mdc-floating-label {
        top: 20px !important;
      }

      :host ::ng-deep .mat-mdc-form-field-icon-prefix,
      :host ::ng-deep .mat-mdc-form-field-icon-suffix {
        padding: 0 6px !important;
      }

      :host ::ng-deep .mat-mdc-form-field-icon-prefix mat-icon,
      :host ::ng-deep .mat-mdc-form-field-icon-suffix mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      :host ::ng-deep input.mat-mdc-input-element {
        font-size: 14px;
      }

      /* Textarea (Beskrivning) - original size */
      :host ::ng-deep .mat-mdc-form-field:has(textarea) .mat-mdc-form-field-flex {
        height: auto !important;
        min-height: 100px !important;
      }

      :host ::ng-deep textarea.mat-mdc-input-element {
        min-height: 80px;
        font-size: 14px;
        padding-top: 12px !important;
        padding-bottom: 12px !important;
      }

      :host ::ng-deep .mat-mdc-form-field-hint-wrapper,
      :host ::ng-deep .mat-mdc-form-field-error-wrapper {
        padding: 0 8px;
      }

      .full-width {
        width: 100%;
      }

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 600;
        color: #29415A;
        margin: 28px 0 20px;
        padding-bottom: 8px;
        border-bottom: 2px solid #ECF1FA;
      }

      .section-title mat-icon {
        color: #1D968C;
        font-size: 20px;
        width: 20px;
        height: 20px;
      }

      .date-time-row {
        display: flex;
        gap: 12px;
        align-items: flex-start;
      }

      .date-field {
        flex: 1;
      }

      .time-field {
        width: 130px;
      }

      .address-row {
        display: flex;
        gap: 12px;
      }

      .postal-code-field {
        width: 130px;
      }

      .city-field {
        flex: 1;
      }

      .validation-error {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 14px;
        background: #FFEBEE;
        border-radius: 6px;
        color: #C62828;
        margin-bottom: 12px;
        font-size: 13px;
      }

      .validation-error mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      .checkbox-row {
        margin: 8px 0;
      }

      .form-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #D0D1D4;
      }

      .form-actions button {
        display: flex;
        align-items: center;
        gap: 6px;
      }

      @media (max-width: 600px) {
        .date-time-row,
        .address-row {
          flex-direction: column;
          gap: 0;
        }

        .date-field,
        .time-field,
        .postal-code-field,
        .city-field {
          width: 100%;
          max-width: none;
        }

        .form-actions {
          flex-direction: column-reverse;
        }

        .form-actions button {
          width: 100%;
          justify-content: center;
        }
      }
    `]
})
export class EventFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private eventService = inject(EventService);
    private snackBar = inject(MatSnackBar);

    // Signals
    loading = signal(false);
    submitting = signal(false);
    isEditMode = signal(false);
    dateError = signal<string | null>(null);

    // Form
    eventForm!: FormGroup;
    private eventId: number | null = null;

    ngOnInit(): void {
        this.initForm();

        // Check if edit mode
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.eventId = parseInt(id, 10);
            this.isEditMode.set(true);
            this.loadEvent();
        } else {
            // Set default times for new events
            this.eventForm.patchValue({
                startTime: '10:00',
                endTime: '16:00'
            });
        }

        // Watch for date changes to validate
        this.eventForm.get('startDate')?.valueChanges.subscribe(() => this.validateDates());
        this.eventForm.get('endDate')?.valueChanges.subscribe(() => this.validateDates());
        this.eventForm.get('startTime')?.valueChanges.subscribe(() => this.validateDates());
        this.eventForm.get('endTime')?.valueChanges.subscribe(() => this.validateDates());
    }

    private initForm(): void {
        this.eventForm = this.fb.group({
            name: ['', [Validators.required, Validators.maxLength(200)]],
            description: ['', Validators.maxLength(2000)],
            startDate: [null, Validators.required],
            startTime: ['10:00', Validators.required],
            endDate: [null, Validators.required],
            endTime: ['16:00', Validators.required],
            streetAddress: ['', Validators.maxLength(200)],
            postalCode: ['', Validators.maxLength(10)],
            city: ['', Validators.maxLength(100)],
            capacity: [null],
            active: [false]
        });
    }

    private loadEvent(): void {
        if (!this.eventId) return;

        this.loading.set(true);
        this.eventService.getEvent(this.eventId).subscribe({
            next: (event) => {
                // Parse dates
                const startDateTime = this.parseBackendDate(event.startDate);
                const endDateTime = this.parseBackendDate(event.endDate);

                this.eventForm.patchValue({
                    name: event.name,
                    description: event.description,
                    startDate: startDateTime,
                    startTime: startDateTime ? this.formatTime(startDateTime) : '10:00',
                    endDate: endDateTime,
                    endTime: endDateTime ? this.formatTime(endDateTime) : '16:00',
                    streetAddress: event.streetAddress,
                    postalCode: event.postalCode,
                    city: event.city,
                    capacity: event.capacity,
                    active: event.active
                });
                this.loading.set(false);
            },
            error: (err) => {
                console.error('Failed to load event:', err);
                this.snackBar.open('Kunde inte ladda event', 'Stäng', {
                    duration: 5000,
                    panelClass: 'error-snackbar'
                });
                this.loading.set(false);
                this.router.navigate(['/events']);
            }
        });
    }

    /**
     * Parse date from backend - handles array format and ISO string
     */
    private parseBackendDate(dateValue: unknown): Date | null {
        if (!dateValue) {
            return null;
        }

        // Handle array format: [2026, 1, 23, 10, 0]
        if (Array.isArray(dateValue)) {
            const [year, month, day, hour = 0, minute = 0] = dateValue;
            return new Date(year, month - 1, day, hour, minute);
        }

        // Handle ISO string format
        if (typeof dateValue === 'string') {
            return new Date(dateValue);
        }

        // Handle Date object
        if (dateValue instanceof Date) {
            return dateValue;
        }

        return null;
    }

    private formatTime(date: Date): string {
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    }

    private validateDates(): void {
        const startDate = this.eventForm.get('startDate')?.value;
        const endDate = this.eventForm.get('endDate')?.value;
        const startTime = this.eventForm.get('startTime')?.value;
        const endTime = this.eventForm.get('endTime')?.value;

        if (startDate && endDate && startTime && endTime) {
            const start = this.combineDateAndTime(startDate, startTime);
            const end = this.combineDateAndTime(endDate, endTime);

            if (end <= start) {
                this.dateError.set('Slutdatum/tid måste vara efter startdatum/tid');
            } else {
                this.dateError.set(null);
            }
        } else {
            this.dateError.set(null);
        }
    }

    private combineDateAndTime(date: Date, time: string): Date {
        const result = new Date(date);
        const [hours, minutes] = time.split(':').map(Number);
        result.setHours(hours, minutes, 0, 0);
        return result;
    }

    private formatDateTimeISO(date: Date, time: string): string {
        const combined = this.combineDateAndTime(date, time);
        const year = combined.getFullYear();
        const month = (combined.getMonth() + 1).toString().padStart(2, '0');
        const day = combined.getDate().toString().padStart(2, '0');
        const hours = combined.getHours().toString().padStart(2, '0');
        const minutes = combined.getMinutes().toString().padStart(2, '0');
        const seconds = '00';

        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    }

    onSubmit(): void {
        if (this.eventForm.invalid || this.dateError()) return;

        const formValue = this.eventForm.value;

        // Generate slug from name
        const slug = this.generateSlug(formValue.name);

        // Combine date and time into ISO format strings
        const eventData = {
            name: formValue.name,
            slug: slug,
            description: formValue.description || null,
            startDate: this.formatDateTimeISO(formValue.startDate, formValue.startTime),
            endDate: this.formatDateTimeISO(formValue.endDate, formValue.endTime),
            streetAddress: formValue.streetAddress || null,
            postalCode: formValue.postalCode || null,
            city: formValue.city || null,
            capacity: formValue.capacity || null,
            active: formValue.active || false
        };

        this.submitting.set(true);

        if (this.isEditMode() && this.eventId) {
            this.eventService.updateEvent(this.eventId, eventData).subscribe({
                next: () => {
                    this.snackBar.open('Event uppdaterat!', 'Stäng', {
                        duration: 3000,
                        panelClass: 'success-snackbar'
                    });
                    this.router.navigate(['/events', this.eventId]);
                },
                error: (err) => {
                    console.error('Failed to update event:', err);
                    this.snackBar.open('Kunde inte uppdatera event', 'Stäng', {
                        duration: 5000,
                        panelClass: 'error-snackbar'
                    });
                    this.submitting.set(false);
                }
            });
        } else {
            this.eventService.createEvent(eventData).subscribe({
                next: (created) => {
                    this.snackBar.open('Event skapat!', 'Stäng', {
                        duration: 3000,
                        panelClass: 'success-snackbar'
                    });
                    this.router.navigate(['/events', created.id]);
                },
                error: (err) => {
                    console.error('Failed to create event:', err);
                    this.snackBar.open('Kunde inte skapa event: ' + (err.error?.message || 'Okänt fel'), 'Stäng', {
                        duration: 5000,
                        panelClass: 'error-snackbar'
                    });
                    this.submitting.set(false);
                }
            });
        }
    }

    private generateSlug(name: string): string {
        return name
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[åä]/g, 'a')
            .replace(/[ö]/g, 'o')
            .replace(/[^a-z0-9\s-]/g, '')
            .replace(/\s+/g, '-')
            .replace(/-+/g, '-')
            .replace(/^-|-$/g, '');
    }
}