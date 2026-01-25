import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PatrolService } from '../../../core/services/patrol.service';
import { Patrol } from '../../../core/models/patrol.model';

@Component({
  selector: 'app-patrol-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatProgressSpinnerModule, MatSnackBarModule],
  template: `
    <div class="page-header">
      <h1>Patruller</h1>
      <button mat-raised-button color="primary" routerLink="/patrols/new">
        <mat-icon>add</mat-icon> Ny patrull
      </button>
    </div>
    @if (loading()) {
      <div class="loading"><mat-spinner diameter="40"></mat-spinner></div>
    } @else {
      <mat-card>
        <table mat-table [dataSource]="patrols()" class="data-table">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Namn</th>
            <td mat-cell *matCellDef="let p">{{ p.name }}</td>
          </ng-container>
          <ng-container matColumnDef="event">
            <th mat-header-cell *matHeaderCellDef>Evenemang</th>
            <td mat-cell *matCellDef="let p">{{ p.eventName || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="participants">
            <th mat-header-cell *matHeaderCellDef>Deltagare</th>
            <td mat-cell *matCellDef="let p">{{ p.participantCount || 0 }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Åtgärder</th>
            <td mat-cell *matCellDef="let p">
              <button mat-icon-button [routerLink]="['/patrols', p.id, 'edit']"><mat-icon>edit</mat-icon></button>
              <button mat-icon-button color="warn" (click)="delete(p)"><mat-icon>delete</mat-icon></button>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>
        </table>
        @if (patrols().length === 0) {
          <div class="empty"><mat-icon>groups</mat-icon><p>Inga patruller</p></div>
        }
      </mat-card>
    }
  `,
  styles: [`.loading, .empty { display: flex; flex-direction: column; align-items: center; padding: 48px; } .data-table { width: 100%; }`]
})
export class PatrolListComponent implements OnInit {
  private readonly service = inject(PatrolService);
  private readonly snackBar = inject(MatSnackBar);
  patrols = signal<Patrol[]>([]);
  loading = signal(true);
  columns = ['name', 'event', 'participants', 'actions'];

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: d => { this.patrols.set(d); this.loading.set(false); },
      error: () => { this.snackBar.open('Fel', 'Stäng', { duration: 3000 }); this.loading.set(false); }
    });
  }

  delete(p: Patrol): void {
    if (confirm(`Ta bort "${p.name}"?`)) {
      this.service.delete(p.id!).subscribe({ next: () => this.load(), error: () => this.snackBar.open('Fel', 'Stäng', { duration: 3000 }) });
    }
  }
}
