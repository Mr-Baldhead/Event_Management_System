import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

// Interface for allergy report data
interface AllergyParticipant {
    participantId: number;
    fullName: string;
    patrol: string;
    otherInfo: string;
}

interface AllergyGroup {
    allergyName: string;
    count: number;
    participants: AllergyParticipant[];
}

interface AllergyReport {
    eventId: number;
    eventName: string;
    allergies: AllergyGroup[];
    totalParticipantsWithAllergies: number;
    generatedAt: string;
}

@Component({
    selector: 'app-allergy-report',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatSnackBarModule,
        HeaderComponent,
        LoadingSpinnerComponent
    ],
    template: `
        <app-header />

        <main class="container">
            <a [routerLink]="['/events', eventId()]" class="back-link">
                <mat-icon>arrow_back</mat-icon>
                Tillbaka till event
            </a>

            @if (loading()) {
                <app-loading-spinner message="Laddar allergirapport..." />
            } @else if (report()) {
                <div class="report-content">
                    <!-- Export buttons -->
                    <div class="export-buttons">
                        <button mat-stroked-button (click)="exportExcel()">
                            Exportera till Excel
                        </button>
                        <button mat-flat-button color="primary" (click)="exportPDF()">
                            Exportera till PDF
                        </button>
                        <button mat-stroked-button (click)="exportCSV()">
                            Exportera till CSV
                        </button>
                    </div>

                    @if (report()!.allergies.length === 0) {
                        <div class="empty-state">
                            <mat-icon>check_circle</mat-icon>
                            <h3>Inga allergier registrerade</h3>
                            <p>Det finns inga deltagare med allergier för detta event.</p>
                        </div>
                    } @else {
                        <!-- Allergy groups -->
                        @for (group of report()!.allergies; track group.allergyName) {
                            <div class="allergy-group">
                                <div class="allergy-header">
                                    {{ group.allergyName }} {{ group.count }} st.
                                </div>
                                <div class="participants-list">
                                    @for (participant of group.participants; track participant.participantId) {
                                        <div class="participant-row">
                                            <span class="name">{{ participant.fullName }}</span>
                                            <span class="patrol">{{ participant.patrol || '' }}</span>
                                        </div>
                                    }
                                </div>
                            </div>
                        }
                    }
                </div>
            } @else {
                <div class="error-state">
                    <mat-icon>error</mat-icon>
                    <h3>Kunde inte ladda rapporten</h3>
                    <p>Något gick fel när rapporten skulle hämtas.</p>
                    <a mat-flat-button color="primary" routerLink="/events">
                        Tillbaka till events
                    </a>
                </div>
            }
        </main>
    `,
    styles: [`
      main {
        max-width: 800px;
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

      .report-content {
        background: white;
        padding: 32px;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      /* Export buttons - matching design */
      .export-buttons {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        margin-bottom: 40px;
      }

      .export-buttons button {
        font-weight: 500;
      }

      /* Allergy groups */
      .allergy-group {
        margin-bottom: 40px;
      }

      .allergy-header {
        background: #416487;
        color: white;
        padding: 14px 24px;
        font-size: 20px;
        font-weight: 600;
        font-style: italic;
      }

      .participants-list {
        padding: 8px 0;
      }

      .participant-row {
        display: flex;
        padding: 10px 24px;
        border-bottom: 1px solid #F0F0F0;
        font-size: 16px;
      }

      .participant-row:last-child {
        border-bottom: none;
      }

      .participant-row .name {
        min-width: 220px;
        color: #1F2023;
      }

      .participant-row .patrol {
        color: #5F6166;
      }

      /* Empty state */
      .empty-state {
        text-align: center;
        padding: 64px 32px;
        color: #82848C;
      }

      .empty-state mat-icon {
        font-size: 72px;
        width: 72px;
        height: 72px;
        color: #4CAF50;
        margin-bottom: 24px;
      }

      .empty-state h3 {
        color: #5F6166;
        margin-bottom: 8px;
        font-size: 20px;
      }

      .empty-state p {
        color: #82848C;
      }

      /* Error state */
      .error-state {
        text-align: center;
        padding: 64px 32px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .error-state mat-icon {
        font-size: 72px;
        width: 72px;
        height: 72px;
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

      @media (max-width: 576px) {
        .report-content {
          padding: 20px;
        }

        .export-buttons {
          flex-direction: column;
        }

        .export-buttons button {
          width: 100%;
        }

        .participant-row {
          flex-direction: column;
          gap: 4px;
          padding: 12px 16px;
        }

        .participant-row .name {
          min-width: auto;
          font-weight: 500;
        }

        .participant-row .patrol {
          font-size: 14px;
        }

        .allergy-header {
          padding: 12px 16px;
          font-size: 18px;
        }
      }

      @media print {
        .export-buttons,
        .back-link,
        app-header {
          display: none !important;
        }

        .report-content {
          box-shadow: none;
          padding: 0;
        }

        .allergy-header {
          background: #416487 !important;
          color: white !important;
          -webkit-print-color-adjust: exact;
          print-color-adjust: exact;
        }
      }
    `]
})
export class AllergyReportComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private http = inject(HttpClient);
    private snackBar = inject(MatSnackBar);

    // State signals
    loading = signal(true);
    eventId = signal<number>(0);
    report = signal<AllergyReport | null>(null);

    ngOnInit(): void {
        const idParam = this.route.snapshot.paramMap.get('id');

        if (idParam) {
            const id = parseInt(idParam, 10);
            if (!isNaN(id)) {
                this.eventId.set(id);
                this.loadReport(id);
            } else {
                this.loading.set(false);
            }
        } else {
            this.loading.set(false);
        }
    }

    private loadReport(eventId: number): void {
        this.loading.set(true);
        this.http.get<AllergyReport>(`/api/events/${eventId}/allergy-report`).subscribe({
            next: (report) => {
                this.report.set(report);
                this.loading.set(false);
            },
            error: (err) => {
                console.error('Failed to load allergy report:', err);
                this.report.set(null);
                this.loading.set(false);
            }
        });
    }

    exportExcel(): void {
        const id = this.eventId();
        window.location.href = `/api/events/${id}/allergy-report/excel`;
    }

    exportPDF(): void {
        window.print();
    }

    exportCSV(): void {
        const id = this.eventId();
        window.location.href = `/api/events/${id}/allergy-report/csv`;
    }
}