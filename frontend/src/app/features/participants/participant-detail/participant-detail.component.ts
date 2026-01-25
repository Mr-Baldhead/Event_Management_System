import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-participant-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <div class="page-header">
      <button mat-icon-button routerLink="/participants"><mat-icon>arrow_back</mat-icon></button>
      <h1>Deltagardetaljer</h1>
    </div>
    <mat-card>
      <mat-card-content>
        <p>Deltagardetaljer kommer snart...</p>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; } h1 { margin: 0; }`]
})
export class ParticipantDetailComponent {}
