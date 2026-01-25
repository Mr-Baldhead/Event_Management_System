import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RegistrationService } from '../../../core/services/registration.service';
import { Registration } from '../../../core/models/registration.model';

@Component({
  selector: 'app-registration-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatProgressSpinnerModule, MatSnackBarModule],
  template: `
    <div class="page-header">
      <h1>Anmälningar</h1>
    </div>
    @if (loading()) {
      <div class="loading"><mat-spinner diameter="40"></mat-spinner></div>
    } @else {
      <mat-card>
        <table mat-table [dataSource]="registrations()" class="data-table">
          <ng-container matColumnDef="event">
            <th mat-header-cell *matHeaderCellDef>Evenemang</th>
            <td mat-cell *matCellDef="let r">{{ r.eventName }}</td>
          </ng-container>
          <ng-container matColumnDef="participant">
            <th mat-header-cell *matHeaderCellDef>Deltagare</th>
            <td mat-cell *matCellDef="let r">{{ r.participantName }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let r">
              <span class="status-badge" [ngClass]="r.status.toLowerCase()">{{ translateStatus(r.status) }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="date">
            <th mat-header-cell *matHeaderCellDef>Datum</th>
            <td mat-cell *matCellDef="let r">{{ formatDate(r.registrationDate) }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Åtgärder</th>
            <td mat-cell *matCellDef="let r">
              @if (r.status === 'PENDING') {
                <button mat-icon-button color="primary" (click)="confirm(r.id)" matTooltip="Bekräfta"><mat-icon>check</mat-icon></button>
              }
              @if (r.status !== 'CANCELLED') {
                <button mat-icon-button color="warn" (click)="cancel(r.id)" matTooltip="Avboka"><mat-icon>close</mat-icon></button>
              }
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>
        </table>
        @if (registrations().length === 0) {
          <div class="empty"><mat-icon>how_to_reg</mat-icon><p>Inga anmälningar</p></div>
        }
      </mat-card>
    }
  `,
  styles: [`
    .loading, .empty { display: flex; flex-direction: column; align-items: center; padding: 48px; }
    .data-table { width: 100%; }
    .status-badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; }
    .pending { background: #fff3e0; color: #e65100; }
    .confirmed { background: #e8f5e9; color: #2e7d32; }
    .cancelled { background: #ffebee; color: #c62828; }
    .waitlisted { background: #e3f2fd; color: #1565c0; }
  `]
})
export class RegistrationListComponent implements OnInit {
  private readonly service = inject(RegistrationService);
  private readonly snackBar = inject(MatSnackBar);
  registrations = signal<Registration[]>([]);
  loading = signal(true);
  columns = ['event', 'participant', 'status', 'date', 'actions'];

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: d => { this.registrations.set(d); this.loading.set(false); },
      error: () => { this.snackBar.open('Fel', 'Stäng', { duration: 3000 }); this.loading.set(false); }
    });
  }

  translateStatus(s: string): string {
    return { PENDING: 'Väntar', CONFIRMED: 'Bekräftad', CANCELLED: 'Avbokad', WAITLISTED: 'Väntelista' }[s] || s;
  }

  formatDate(d: string): string {
    return d ? new Date(d).toLocaleDateString('sv-SE') : '-';
  }

  confirm(id: number): void {
    this.service.confirm(id).subscribe({ next: () => { this.snackBar.open('Bekräftad', 'Stäng', { duration: 3000 }); this.load(); } });
  }

  cancel(id: number): void {
    if (confirm('Avboka?')) {
      this.service.cancel(id).subscribe({ next: () => { this.snackBar.open('Avbokad', 'Stäng', { duration: 3000 }); this.load(); } });
    }
  }
}
