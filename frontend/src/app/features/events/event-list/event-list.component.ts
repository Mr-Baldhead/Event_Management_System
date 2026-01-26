import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EventService } from '../../../core/services/event.service';
import { AuthService } from '../../../core/services/auth.service';
import { Event, formatDate } from '../../../shared/models/event.model';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
    selector: 'app-event-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatDialogModule,
        MatSnackBarModule,
        MatChipsModule,
        MatSlideToggleModule,
        MatTooltipModule,
        HeaderComponent,
        LoadingSpinnerComponent
    ],
    template: `
        <app-header />

        <main class="container">
            <div class="page-header">
                <h1>Events</h1>
                @if (authService.isAdmin()) {
                    <a mat-flat-button color="primary" routerLink="/events/new">
                        <mat-icon>add</mat-icon>
                        Skapa nytt event
                    </a>
                }
            </div>

            @if (eventService.loading()) {
                <app-loading-spinner message="Laddar events..." />
            } @else if (!eventService.hasEvents()) {
                <div class="empty-state card">
                    <mat-icon class="empty-icon">event_busy</mat-icon>
                    <h3>Inga events hittades</h3>
                    <p>Skapa ditt första event för att komma igång!</p>
                    @if (authService.isAdmin()) {
                        <a mat-flat-button color="primary" routerLink="/events/new">
                            <mat-icon>add</mat-icon>
                            Skapa nytt event
                        </a>
                    }
                </div>
            } @else {
                <div class="events-grid">
                    @for (event of eventService.events(); track event.id) {
                        <mat-card class="event-card" [class.inactive]="!event.active">
                            <mat-card-header>
                                <mat-card-title>{{ event.name }}</mat-card-title>
                                @if (!event.active) {
                                    <mat-chip class="status-chip inactive">Inaktiv</mat-chip>
                                }
                            </mat-card-header>

                            <mat-card-content>
                                <div class="event-info">
                                    <div class="info-row">
                                        <mat-icon>calendar_today</mat-icon>
                                        <span>{{ formatDate(event.startDate) }} - {{ formatDate(event.endDate) }}</span>
                                    </div>

                                    <div class="info-row">
                                        <mat-icon>location_on</mat-icon>
                                        <span>{{ event.city || 'Plats ej angiven' }}</span>
                                    </div>

                                    <div class="info-row">
                                        <mat-icon>people</mat-icon>
                                        <span>
                      {{ event.registrationCount }} registrerade
                                            @if (event.capacity) {
                                                / {{ event.capacity }} platser
                                            }
                    </span>
                                    </div>

                                    @if (event.capacity && event.remainingSpots !== undefined) {
                                        <div class="info-row">
                                            <mat-icon>event_seat</mat-icon>
                                            <span [class.warning]="event.remainingSpots < 10">
                        {{ event.remainingSpots }} platser kvar
                      </span>
                                        </div>
                                    }
                                </div>
                            </mat-card-content>

                            <mat-card-actions align="end">
                                <!-- Active toggle - only for admins -->
                                <mat-slide-toggle
                                        *ngIf="authService.isAdmin()"
                                        [checked]="event.active"
                                        (change)="toggleActive(event)"
                                        [matTooltip]="event.active ? 'Inaktivera event' : 'Aktivera event'"
                                        color="primary"
                                        class="active-toggle">
                                </mat-slide-toggle>

                                <a mat-button color="primary" [routerLink]="['/events', event.id]">
                                    <mat-icon>visibility</mat-icon>
                                    Visa
                                </a>

                                <ng-container *ngIf="authService.isAdmin()">
                                    <a mat-button color="accent" [routerLink]="['/events', event.id, 'edit']">
                                        <mat-icon>edit</mat-icon>
                                        Redigera
                                    </a>

                                    <button mat-button color="warn" (click)="confirmDelete(event)">
                                        <mat-icon>delete</mat-icon>
                                        Ta bort
                                    </button>
                                </ng-container>
                            </mat-card-actions>
                        </mat-card>
                    }
                </div>
            }
        </main>
    `,
    styles: [`
      main {
        max-width: 1200px;
        margin: 0 auto;
        padding: 32px 24px;
      }

      .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 32px;
        flex-wrap: wrap;
        gap: 16px;
      }

      .page-header h1 {
        margin: 0;
      }

      .events-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
        gap: 24px;
      }

      .event-card {
        transition: transform 0.25s ease, box-shadow 0.25s ease;
      }

      .event-card:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      }

      .event-card.inactive {
        opacity: 0.7;
      }

      .event-card mat-card-header {
        margin-bottom: 16px;
      }

      .event-card mat-card-title {
        font-size: 20px;
        font-weight: 600;
      }

      .status-chip {
        margin-left: auto;
        font-size: 12px;
      }

      .status-chip.inactive {
        background: #A8AAB0;
        color: #5F6166;
      }

      .event-info {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .info-row {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #5F6166;
        font-size: 14px;
      }

      .info-row mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        color: #1D968C;
      }

      .info-row .warning {
        color: #ffc107;
        font-weight: 500;
      }

      mat-card-actions {
        display: flex;
        align-items: center;
        margin-top: 16px;
        padding: 8px 0 0;
        border-top: 1px solid #D0D1D4;
      }

      mat-card-actions button,
      mat-card-actions a {
        display: inline-flex;
        align-items: center;
        gap: 4px;
      }

      /* Active toggle styling */
      .active-toggle {
        margin-right: 8px;
      }

      :host ::ng-deep .active-toggle .mdc-switch {
        --mdc-switch-selected-track-color: #1D968C;
        --mdc-switch-selected-handle-color: #1D968C;
        --mdc-switch-selected-hover-track-color: #136E67;
        --mdc-switch-selected-focus-track-color: #1D968C;
        --mdc-switch-selected-pressed-track-color: #136E67;
      }

      .empty-state {
        text-align: center;
        padding: 48px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .empty-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #A8AAB0;
        margin-bottom: 24px;
      }

      .empty-state h3 {
        color: #5F6166;
        margin-bottom: 8px;
      }

      .empty-state p {
        color: #82848C;
        margin-bottom: 24px;
      }
    `]
})
export class EventListComponent implements OnInit {
    eventService = inject(EventService);
    authService = inject(AuthService);
    private dialog = inject(MatDialog);
    private snackBar = inject(MatSnackBar);

    formatDate = formatDate;

    ngOnInit(): void {
        this.eventService.loadEvents();
    }

    /**
     * Toggle event active status
     */
    toggleActive(event: Event): void {
        const newActiveState = !event.active;

        this.eventService.patchEvent(event.id, { active: newActiveState }).subscribe({
            next: () => {
                this.snackBar.open(
                    newActiveState ? 'Event aktiverat' : 'Event inaktiverat',
                    'Stäng',
                    { duration: 2000, panelClass: 'success-snackbar' }
                );
            },
            error: () => {
                this.snackBar.open('Kunde inte ändra status', 'Stäng', {
                    duration: 5000,
                    panelClass: 'error-snackbar'
                });
            }
        });
    }

    confirmDelete(event: Event): void {
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
            data: {
                title: 'Ta bort event?',
                itemName: event.name,
                message: 'Eventet och alla dess registreringar kommer att tas bort permanent.',
                confirmText: 'Ja, ta bort',
                cancelText: 'Avbryt',
                confirmColor: 'warn',
                icon: 'delete_forever'
            } as ConfirmDialogData
        });

        dialogRef.afterClosed().subscribe(confirmed => {
            if (confirmed) {
                this.deleteEvent(event.id);
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
            },
            error: () => {
                this.snackBar.open('Kunde inte ta bort event', 'Stäng', {
                    duration: 5000,
                    panelClass: 'error-snackbar'
                });
            }
        });
    }
}