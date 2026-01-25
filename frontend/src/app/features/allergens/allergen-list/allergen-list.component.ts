import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AllergenService } from '../../../core/services/allergen.service';
import { Allergen } from '../../../core/models/allergen.model';

@Component({
  selector: 'app-allergen-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatDialogModule, MatProgressSpinnerModule, MatSnackBarModule],
  template: `
    <div class="page-header">
      <h1>Allergener</h1>
      <button mat-raised-button color="primary" (click)="openDialog()">
        <mat-icon>add</mat-icon> Ny allergen
      </button>
    </div>
    @if (loading()) {
      <div class="loading"><mat-spinner diameter="40"></mat-spinner></div>
    } @else {
      <mat-card>
        <table mat-table [dataSource]="allergens()" class="data-table">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Namn</th>
            <td mat-cell *matCellDef="let a">{{ a.name }}</td>
          </ng-container>
          <ng-container matColumnDef="severity">
            <th mat-header-cell *matHeaderCellDef>Allvarlighetsgrad</th>
            <td mat-cell *matCellDef="let a">
              <span class="severity-badge" [ngClass]="a.severity.toLowerCase()">{{ translateSeverity(a.severity) }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef>Beskrivning</th>
            <td mat-cell *matCellDef="let a">{{ a.description || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="participants">
            <th mat-header-cell *matHeaderCellDef>Antal</th>
            <td mat-cell *matCellDef="let a">{{ a.participantCount || 0 }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Åtgärder</th>
            <td mat-cell *matCellDef="let a">
              <button mat-icon-button color="warn" (click)="delete(a)"><mat-icon>delete</mat-icon></button>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>
        </table>
        @if (allergens().length === 0) {
          <div class="empty"><mat-icon>warning</mat-icon><p>Inga allergener</p></div>
        }
      </mat-card>
    }
  `,
  styles: [`
    .loading, .empty { display: flex; flex-direction: column; align-items: center; padding: 48px; }
    .data-table { width: 100%; }
    .severity-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; }
    .low { background: #e8f5e9; color: #2e7d32; }
    .medium { background: #fff3e0; color: #e65100; }
    .high { background: #ffebee; color: #c62828; }
    .critical { background: #c62828; color: white; }
  `]
})
export class AllergenListComponent implements OnInit {
  private readonly service = inject(AllergenService);
  private readonly snackBar = inject(MatSnackBar);
  allergens = signal<Allergen[]>([]);
  loading = signal(true);
  columns = ['name', 'severity', 'description', 'participants', 'actions'];

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: d => { this.allergens.set(d); this.loading.set(false); },
      error: () => { this.snackBar.open('Fel', 'Stäng', { duration: 3000 }); this.loading.set(false); }
    });
  }

  translateSeverity(s: string): string {
    return { LOW: 'Låg', MEDIUM: 'Medel', HIGH: 'Hög', CRITICAL: 'Kritisk' }[s] || s;
  }

  openDialog(): void {
    // For simplicity, use prompt
    const name = prompt('Allergennamn:');
    if (name) {
      this.service.create({ name, severity: 'MEDIUM' }).subscribe({ next: () => this.load() });
    }
  }

  delete(a: Allergen): void {
    if (confirm(`Ta bort "${a.name}"?`)) {
      this.service.delete(a.id!).subscribe({ next: () => this.load() });
    }
  }
}
