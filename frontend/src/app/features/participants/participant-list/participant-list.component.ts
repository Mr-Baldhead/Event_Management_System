import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ParticipantService } from '../../../core/services/participant.service';
import { Participant } from '../../../core/models/participant.model';

@Component({
  selector: 'app-participant-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="page-header">
      <h1>Deltagare</h1>
      <button mat-raised-button color="primary" routerLink="/participants/new">
        <mat-icon>add</mat-icon>
        Lägg till deltagare
      </button>
    </div>

    @if (loading()) {
      <div class="loading-container">
        <mat-spinner diameter="40"></mat-spinner>
      </div>
    } @else {
      <mat-card>
        <table mat-table [dataSource]="participants()" class="data-table">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Namn</th>
            <td mat-cell *matCellDef="let p">
              <a [routerLink]="['/participants', p.id]" class="link">
                {{ p.firstName }} {{ p.lastName }}
              </a>
            </td>
          </ng-container>

          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>E-post</th>
            <td mat-cell *matCellDef="let p">{{ p.email || '-' }}</td>
          </ng-container>

          <ng-container matColumnDef="patrol">
            <th mat-header-cell *matHeaderCellDef>Patrull</th>
            <td mat-cell *matCellDef="let p">{{ p.patrolName || '-' }}</td>
          </ng-container>

          <ng-container matColumnDef="allergens">
            <th mat-header-cell *matHeaderCellDef>Allergier</th>
            <td mat-cell *matCellDef="let p">
              @if (p.allergens && p.allergens.length > 0) {
                <mat-chip-set>
                  @for (a of p.allergens; track a.id) {
                    <mat-chip [ngClass]="'severity-' + a.severity.toLowerCase()">{{ a.name }}</mat-chip>
                  }
                </mat-chip-set>
              } @else {
                -
              }
            </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Åtgärder</th>
            <td mat-cell *matCellDef="let p">
              <button mat-icon-button [routerLink]="['/participants', p.id, 'edit']">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteParticipant(p)">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>

        @if (participants().length === 0) {
          <div class="empty-state">
            <mat-icon>people</mat-icon>
            <p>Inga deltagare hittades</p>
          </div>
        }
      </mat-card>
    }
  `,
  styles: [`
    .loading-container { display: flex; justify-content: center; padding: 48px; }
    .data-table { width: 100%; }
    .link { color: #416487; text-decoration: none; font-weight: 500; }
    .link:hover { text-decoration: underline; }
    .empty-state { display: flex; flex-direction: column; align-items: center; padding: 48px; color: #82848C; }
    .empty-state mat-icon { font-size: 64px; width: 64px; height: 64px; margin-bottom: 16px; }
    .severity-low { background-color: #e8f5e9 !important; }
    .severity-medium { background-color: #fff3e0 !important; }
    .severity-high { background-color: #ffebee !important; }
    .severity-critical { background-color: #c62828 !important; color: white !important; }
  `]
})
export class ParticipantListComponent implements OnInit {
  private readonly service = inject(ParticipantService);
  private readonly snackBar = inject(MatSnackBar);

  participants = signal<Participant[]>([]);
  loading = signal(true);
  displayedColumns = ['name', 'email', 'patrol', 'allergens', 'actions'];

  ngOnInit(): void {
    this.loadParticipants();
  }

  loadParticipants(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (data) => { this.participants.set(data); this.loading.set(false); },
      error: () => { this.snackBar.open('Kunde inte ladda deltagare', 'Stäng', { duration: 3000 }); this.loading.set(false); }
    });
  }

  deleteParticipant(p: Participant): void {
    if (confirm(`Ta bort "${p.firstName} ${p.lastName}"?`)) {
      this.service.delete(p.id!).subscribe({
        next: () => { this.snackBar.open('Deltagaren har tagits bort', 'Stäng', { duration: 3000 }); this.loadParticipants(); },
        error: () => { this.snackBar.open('Kunde inte ta bort deltagaren', 'Stäng', { duration: 3000 }); }
      });
    }
  }
}
