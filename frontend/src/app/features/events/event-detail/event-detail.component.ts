import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

// Interface for registration data
interface Registration {
    id: number;
    participantId: number;
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    streetAddress: string;
    postalCode: string;
    city: string;
    patrolName: string;
    registrationDate: string;
    guardianName?: string;
}

@Component({
    selector: 'app-event-detail',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        FormsModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatDialogModule,
        MatSnackBarModule,
        MatDividerModule,
        MatCheckboxModule,
        MatTooltipModule,
        HeaderComponent,
        LoadingSpinnerComponent
    ],
    template: `
        <app-header />

        <main class="container">
            <a routerLink="/events" class="back-link">
                <mat-icon>arrow_back</mat-icon>
                Tillbaka till alla events
            </a>

            @if (loading()) {
                <app-loading-spinner message="Laddar event..." />
            } @else if (event()) {
                <mat-card class="event-card">
                    <mat-card-header>
                        <mat-card-title>{{ event()!.name }}</mat-card-title>
                    </mat-card-header>

                    <mat-card-content>
                        <div class="info-grid">
                            <div class="info-item">
                                <span class="label">📅 Startdatum</span>
                                <span class="value">{{ formatDateValue(event()!.startDate) }}</span>
                            </div>

                            <div class="info-item">
                                <span class="label">📅 Slutdatum</span>
                                <span class="value">{{ formatDateValue(event()!.endDate) }}</span>
                            </div>

                            <div class="info-item">
                                <span class="label">📍 Plats</span>
                                <span class="value">
                  @if (event()!.streetAddress) {
                      {{ event()!.streetAddress }},
                  }
                                    {{ event()!.postalCode }} {{ event()!.city }}
                </span>
                            </div>

                            <div class="info-item">
                                <span class="label">👥 Max deltagare</span>
                                <span class="value">{{ event()!.capacity || 'Obegränsat' }}</span>
                            </div>
                        </div>

                        @if (event()!.description) {
                            <div class="description-section">
                                <h3>Beskrivning</h3>
                                <p>{{ event()!.description }}</p>
                            </div>
                        }

                        <!-- Action buttons -->
                        <div class="action-buttons">
                            <div class="spacer"></div>
                            <a mat-stroked-button
                               color="primary"
                               [routerLink]="['/events', event()!.id, 'allergy-report']">
                                <mat-icon>medical_services</mat-icon>
                                Allergi-rapport
                            </a>

                            @if (authService.isAdmin()) {
                                <a mat-flat-button
                                   color="primary"
                                   [routerLink]="['/events', event()!.id, 'edit']">
                                    <mat-icon>edit</mat-icon>
                                    Redigera
                                </a>

                                <button mat-flat-button color="warn" (click)="confirmDeleteEvent()">
                                    <mat-icon>delete</mat-icon>
                                    Ta bort
                                </button>
                            }
                        </div>

                        <mat-divider />

                        <!-- Registrations section -->
                        <div class="registrations-section">
                            <h3>
                                <mat-icon>people</mat-icon>
                                Registreringar ({{ event()!.registrationCount || 0 }})
                            </h3>

                            @if (!event()!.registrationCount || event()!.registrationCount === 0) {
                                <div class="empty-registrations">
                                    <mat-icon>person_off</mat-icon>
                                    <p>Inga registreringar ännu</p>
                                </div>
                            } @else {
                                <p class="info-text">
                                    {{ event()!.registrationCount }} deltagare registrerade.
                                    @if (event()!.remainingSpots !== undefined && event()!.remainingSpots !== null) {
                                        {{ event()!.remainingSpots }} platser kvar.
                                    }
                                </p>

                                <!-- Export buttons and column toggles -->
                                <div class="list-toolbar">
                                    <div class="export-buttons">
                                        <button mat-stroked-button (click)="exportExcel()">
                                            <mat-icon>table_chart</mat-icon>
                                            Exportera till Excel
                                        </button>
                                        <button mat-stroked-button (click)="exportPDF()">
                                            <mat-icon>picture_as_pdf</mat-icon>
                                            Exportera till PDF
                                        </button>
                                    </div>
                                    <div class="column-toggles">
                                        <span class="toggle-label">Visa:</span>
                                        <mat-checkbox [(ngModel)]="showEmail" color="primary">E-post</mat-checkbox>
                                        <mat-checkbox [(ngModel)]="showPhone" color="primary">Mobil</mat-checkbox>
                                    </div>
                                </div>

                                @if (loadingRegistrations()) {
                                    <app-loading-spinner message="Laddar deltagare..." />
                                } @else if (registrations().length > 0) {
                                    <div class="participants-table">
                                        <table>
                                            <thead>
                                            <tr>
                                                <th>Namn</th>
                                                <th>Adress</th>
                                                <th>Kår/Patrull</th>
                                                @if (showEmail) {
                                                    <th>E-post</th>
                                                }
                                                @if (showPhone) {
                                                    <th>Mobil</th>
                                                }
                                                <th class="action-col"></th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                                @for (reg of registrations(); track reg.id) {
                                                    <tr>
                                                        <td class="name-cell">
                                                            <div class="name">{{ reg.lastName }} {{ reg.firstName }}</div>
                                                            @if (reg.guardianName) {
                                                                <div class="guardian">
                                                                    <mat-icon>family_restroom</mat-icon>
                                                                    {{ reg.guardianName }}
                                                                </div>
                                                            }
                                                        </td>
                                                        <td class="address-cell">
                                                            @if (reg.streetAddress || reg.postalCode || reg.city) {
                                                                <div class="address">
                                                                    @if (reg.streetAddress) {
                                                                        {{ reg.streetAddress }}<br>
                                                                    }
                                                                    {{ reg.postalCode }} {{ reg.city }}
                                                                </div>
                                                            } @else {
                                                                <span class="no-data">-</span>
                                                            }
                                                        </td>
                                                        <td>{{ reg.patrolName || '-' }}</td>
                                                        @if (showEmail) {
                                                            <td class="email-cell">
                                                                @if (reg.email) {
                                                                    <a href="mailto:{{ reg.email }}" class="email-link">{{ reg.email }}</a>
                                                                } @else {
                                                                    <span class="no-data">-</span>
                                                                }
                                                            </td>
                                                        }
                                                        @if (showPhone) {
                                                            <td class="phone-cell">
                                                                @if (reg.phone) {
                                                                    <a href="tel:{{ reg.phone }}" class="phone-link">{{ reg.phone }}</a>
                                                                } @else {
                                                                    <span class="no-data">-</span>
                                                                }
                                                            </td>
                                                        }
                                                        <td class="action-cell">
                                                            <button mat-icon-button
                                                                    color="warn"
                                                                    matTooltip="Ta bort deltagare"
                                                                    (click)="confirmDeleteRegistration(reg)">
                                                                <mat-icon>delete</mat-icon>
                                                            </button>
                                                        </td>
                                                    </tr>
                                                }
                                            </tbody>
                                        </table>
                                    </div>
                                }
                            }
                        </div>
                    </mat-card-content>
                </mat-card>
            } @else {
                <div class="error-state card">
                    <mat-icon>error</mat-icon>
                    <h3>Event hittades inte</h3>
                    <p>Eventet du letar efter finns inte eller har tagits bort.</p>
                    <a mat-flat-button color="primary" routerLink="/events">
                        Tillbaka till events
                    </a>
                </div>
            }
        </main>
    `,
    styles: [`
      main {
        max-width: 1100px;
        margin: 0 auto;
        padding: 32px 24px;
      }

      .back-link {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: #1D968C;
        text-decoration: none;
        font-weight: 500;
        margin-bottom: 24px;
      }

      .back-link:hover {
        text-decoration: underline;
      }

      .event-card mat-card-header {
        margin-bottom: 24px;
      }

      .event-card mat-card-title {
        font-size: 28px;
        font-weight: 600;
      }

      .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;
        margin-bottom: 32px;
      }

      @media (max-width: 576px) {
        .info-grid {
          grid-template-columns: 1fr;
        }
      }

      .info-item {
        padding: 16px;
        background: #F0F1F1;
        border-radius: 8px;
      }

      .info-item .label {
        display: block;
        font-size: 12px;
        color: #82848C;
        text-transform: uppercase;
        margin-bottom: 4px;
      }

      .info-item .value {
        font-size: 18px;
        color: #1F2023;
        font-weight: 500;
      }

      .description-section {
        margin-bottom: 32px;
      }

      .description-section h3 {
        font-size: 20px;
        font-weight: 600;
        color: #1F2023;
        margin-bottom: 16px;
      }

      .description-section p {
        color: #5F6166;
        line-height: 1.6;
      }

      .action-buttons {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        margin: 24px 0;
        align-items: center;
      }

      .action-buttons .spacer {
        flex: 1;
      }

      .action-buttons button,
      .action-buttons a {
        display: inline-flex;
        align-items: center;
        gap: 6px;
      }

      mat-divider {
        margin: 32px 0;
      }

      .registrations-section h3 {
        font-size: 20px;
        font-weight: 600;
        color: #1F2023;
        margin-bottom: 16px;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .registrations-section h3 mat-icon {
        color: #1D968C;
      }

      .empty-registrations {
        text-align: center;
        padding: 32px;
        color: #82848C;
      }

      .empty-registrations mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        margin-bottom: 16px;
      }

      .info-text {
        color: #5F6166;
        margin-bottom: 16px;
      }

      /* List toolbar with export and column toggles */
      .list-toolbar {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        align-items: center;
        gap: 16px;
        margin-bottom: 16px;
        padding: 16px;
        background: #F8F9FA;
        border-radius: 8px;
      }

      .export-buttons {
        display: flex;
        gap: 12px;
      }

      .export-buttons button {
        display: inline-flex;
        align-items: center;
        gap: 6px;
      }

      .column-toggles {
        display: flex;
        align-items: center;
        gap: 16px;
      }

      .toggle-label {
        font-weight: 500;
        color: #5F6166;
        font-size: 14px;
      }

      /* Participants table */
      .participants-table {
        overflow-x: auto;
        margin-top: 16px;
      }

      .participants-table table {
        width: 100%;
        border-collapse: collapse;
        font-size: 14px;
      }

      .participants-table th {
        text-align: left;
        padding: 12px 16px;
        background: #ECF1FA;
        color: #29415A;
        font-weight: 600;
        border-bottom: 2px solid #BFD3EE;
        white-space: nowrap;
      }

      .participants-table th.action-col {
        width: 48px;
      }

      .participants-table td {
        padding: 12px 16px;
        border-bottom: 1px solid #E0E0E0;
        vertical-align: top;
      }

      .participants-table tbody tr:hover {
        background: #F8F9FA;
      }

      .name-cell .name {
        font-weight: 500;
        color: #1F2023;
      }

      .name-cell .guardian {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #82848C;
        margin-top: 4px;
      }

      .name-cell .guardian mat-icon {
        font-size: 14px;
        width: 14px;
        height: 14px;
      }

      .address-cell .address {
        font-size: 13px;
        color: #5F6166;
        line-height: 1.4;
      }

      .no-data {
        color: #A8AAB0;
      }

      .email-cell, .phone-cell {
        white-space: nowrap;
      }

      .email-link, .phone-link {
        color: #1D968C;
        text-decoration: none;
      }

      .email-link:hover, .phone-link:hover {
        text-decoration: underline;
      }

      .action-cell {
        text-align: center;
        vertical-align: middle !important;
      }

      .action-cell button {
        opacity: 0.6;
        transition: opacity 0.2s;
      }

      .participants-table tbody tr:hover .action-cell button {
        opacity: 1;
      }

      @media (max-width: 768px) {
        .participants-table table {
          font-size: 13px;
        }

        .participants-table th,
        .participants-table td {
          padding: 8px 12px;
        }

        .list-toolbar {
          flex-direction: column;
          align-items: flex-start;
        }

        .export-buttons {
          width: 100%;
        }

        .export-buttons button {
          flex: 1;
        }

        .column-toggles {
          width: 100%;
        }
      }

      @media (max-width: 576px) {
        .action-buttons {
          flex-direction: column;
        }

        .action-buttons .spacer {
          display: none;
        }

        .action-buttons button,
        .action-buttons a {
          width: 100%;
          justify-content: center;
        }

        .export-buttons {
          flex-direction: column;
        }
      }

      .error-state {
        text-align: center;
        padding: 48px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .error-state mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #dc3545;
        margin-bottom: 24px;
      }

      .error-state h3 {
        color: #5F6166;
        margin-bottom: 8px;
      }

      .error-state p {
        color: #82848C;
        margin-bottom: 24px;
      }

      /* Print styles - only show participants table */
      @media print {
        app-header,
        .back-link,
        .event-card mat-card-header,
        .info-grid,
        .description-section,
        .action-buttons,
        mat-divider,
        .list-toolbar,
        .registrations-section h3,
        .info-text,
        .empty-registrations,
        .action-cell,
        .action-col,
        .error-state {
          display: none !important;
        }

        main {
          max-width: 100%;
          padding: 0;
          margin: 0;
        }

        .event-card {
          box-shadow: none !important;
          border: none !important;
        }

        mat-card-content {
          padding: 0 !important;
        }

        .registrations-section {
          margin: 0;
        }

        .participants-table {
          margin: 0;
        }

        .participants-table table {
          width: 100%;
          font-size: 11px;
        }

        .participants-table th {
          background: #416487 !important;
          color: white !important;
          -webkit-print-color-adjust: exact;
          print-color-adjust: exact;
          padding: 8px 10px;
        }

        .participants-table td {
          padding: 6px 10px;
          border-bottom: 1px solid #ccc;
        }

        .name-cell .guardian {
          font-size: 10px;
        }

        .address-cell .address {
          font-size: 10px;
        }
      }
    `]
})
export class EventDetailComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private dialog = inject(MatDialog);
    private snackBar = inject(MatSnackBar);
    private http = inject(HttpClient);

    eventService = inject(EventService);
    authService = inject(AuthService);

    // State signals
    loading = signal(true);
    loadingRegistrations = signal(false);
    event = signal<any>(null);
    registrations = signal<Registration[]>([]);

    // Column visibility
    showEmail = false;
    showPhone = false;

    ngOnInit(): void {
        const idParam = this.route.snapshot.paramMap.get('id');

        if (idParam) {
            const eventId = parseInt(idParam, 10);
            if (!isNaN(eventId)) {
                this.loadEvent(eventId);
            } else {
                this.loading.set(false);
            }
        } else {
            this.loading.set(false);
        }
    }

    private loadEvent(id: number): void {
        this.loading.set(true);
        this.eventService.getEvent(id).subscribe({
            next: (event) => {
                this.event.set(event);
                this.loading.set(false);
                if (event.registrationCount > 0) {
                    this.loadRegistrations(id);
                }
            },
            error: (err) => {
                console.error('Failed to load event:', err);
                this.event.set(null);
                this.loading.set(false);
            }
        });
    }

    private loadRegistrations(eventId: number): void {
        this.loadingRegistrations.set(true);
        this.http.get<Registration[]>(`/api/events/${eventId}/registrations`).subscribe({
            next: (registrations) => {
                this.registrations.set(registrations);
                this.loadingRegistrations.set(false);
            },
            error: (err) => {
                console.error('Failed to load registrations:', err);
                this.loadingRegistrations.set(false);
            }
        });
    }

    formatDateValue(dateValue: unknown): string {
        if (!dateValue) {
            return '-';
        }

        let date: Date;

        if (Array.isArray(dateValue)) {
            const [year, month, day, hour = 0, minute = 0] = dateValue;
            date = new Date(year, month - 1, day, hour, minute);
        } else if (typeof dateValue === 'string') {
            date = new Date(dateValue);
        } else if (dateValue instanceof Date) {
            date = dateValue;
        } else {
            return '-';
        }

        return date.toLocaleString('sv-SE', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    // Export functions
    exportExcel(): void {
        const eventId = this.event()?.id;
        if (eventId) {
            window.location.href = `/api/events/${eventId}/registrations/excel`;
        }
    }

    exportPDF(): void {
        window.print();
    }

    // Delete event
    confirmDeleteEvent(): void {
        const currentEvent = this.event();
        if (!currentEvent) return;

        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
            data: {
                title: 'Ta bort event?',
                itemName: currentEvent.name,
                message: 'Eventet och alla dess registreringar kommer att tas bort permanent.',
                confirmText: 'Ja, ta bort',
                cancelText: 'Avbryt',
                confirmColor: 'warn',
                icon: 'delete_forever'
            } as ConfirmDialogData
        });

        dialogRef.afterClosed().subscribe(confirmed => {
            if (confirmed) {
                this.deleteEvent(currentEvent.id);
            }
        });
    }

    private deleteEvent(id: number): void {
        this.eventService.deleteEvent(id).subscribe({
            next: () => {
                this.snackBar.open('Event borttaget', 'Stäng', {
                    duration: 3000,
                    panelClass: 'success-snackbar'
                });
                this.router.navigate(['/events']);
            },
            error: () => {
                this.snackBar.open('Kunde inte ta bort event', 'Stäng', {
                    duration: 5000,
                    panelClass: 'error-snackbar'
                });
            }
        });
    }

    // Delete registration
    confirmDeleteRegistration(reg: Registration): void {
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
            data: {
                title: 'Ta bort deltagare?',
                itemName: `${reg.lastName} ${reg.firstName}`,
                message: 'Deltagaren kommer att tas bort från detta event.',
                confirmText: 'Ja, ta bort',
                cancelText: 'Avbryt',
                confirmColor: 'warn',
                icon: 'person_remove'
            } as ConfirmDialogData
        });

        dialogRef.afterClosed().subscribe(confirmed => {
            if (confirmed) {
                this.deleteRegistration(reg.id);
            }
        });
    }

    private deleteRegistration(registrationId: number): void {
        const eventId = this.event()?.id;
        if (!eventId) return;

        this.http.delete(`/api/events/${eventId}/registrations/${registrationId}`).subscribe({
            next: () => {
                // Remove from local list
                const current = this.registrations();
                this.registrations.set(current.filter(r => r.id !== registrationId));

                // Update event count
                const currentEvent = this.event();
                if (currentEvent) {
                    currentEvent.registrationCount = (currentEvent.registrationCount || 1) - 1;
                    if (currentEvent.remainingSpots !== undefined) {
                        currentEvent.remainingSpots = (currentEvent.remainingSpots || 0) + 1;
                    }
                    this.event.set({ ...currentEvent });
                }

                this.snackBar.open('Deltagare borttagen', 'Stäng', {
                    duration: 3000,
                    panelClass: 'success-snackbar'
                });
            },
            error: () => {
                this.snackBar.open('Kunde inte ta bort deltagare', 'Stäng', {
                    duration: 5000,
                    panelClass: 'error-snackbar'
                });
            }
        });
    }
}