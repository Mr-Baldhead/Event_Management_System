import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';

/**
 * Application header component with navigation and user menu.
 * Displays logo, navigation links, and user profile/logout options.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule
  ],
  template: `
    <header class="header">
      <div class="header-content">
        <!-- Logo and app name -->
        <a routerLink="/" class="logo">
          <mat-icon>event</mat-icon>
          <span>Event Manager</span>
        </a>

        <!-- User section -->
        @if (authService.isLoggedIn()) {
          <div class="user-section">
            <button mat-button [matMenuTriggerFor]="userMenu" class="user-button">
              <mat-icon>account_circle</mat-icon>
              <span class="user-name">{{ authService.userName() }}</span>
              <mat-icon>arrow_drop_down</mat-icon>
            </button>
            
            <mat-menu #userMenu="matMenu">
              <div class="menu-header">
                <span class="menu-name">{{ authService.userName() }}</span>
                <span class="menu-role">{{ authService.currentUser()?.role }}</span>
              </div>
              <mat-divider></mat-divider>
              <button mat-menu-item routerLink="/profile">
                <mat-icon>person</mat-icon>
                <span>Profil</span>
              </button>
              <button mat-menu-item routerLink="/settings">
                <mat-icon>settings</mat-icon>
                <span>Inställningar</span>
              </button>
              <mat-divider></mat-divider>
              <button mat-menu-item (click)="logout()">
                <mat-icon>logout</mat-icon>
                <span>Logga ut</span>
              </button>
            </mat-menu>
          </div>
        } @else {
          <div class="auth-buttons">
            <button mat-button routerLink="/login">
              <mat-icon>login</mat-icon>
              Logga in
            </button>
          </div>
        }
      </div>
    </header>
  `,
  styles: [`
    .header {
      background: white;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      padding: 0;
      height: 64px;
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .header-content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 24px;
      height: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #1D968C;
      text-decoration: none;
      font-weight: 600;
      font-size: 18px;
      transition: opacity 0.2s ease;

      &:hover {
        opacity: 0.8;
      }

      mat-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
      }
    }

    .user-section {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .user-button {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #5F6166;
    }

    .user-name {
      max-width: 150px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .menu-header {
      padding: 12px 16px;
      display: flex;
      flex-direction: column;
      gap: 4px;

      .menu-name {
        font-weight: 500;
        color: #1F2023;
      }

      .menu-role {
        font-size: 12px;
        color: #82848C;
        text-transform: uppercase;
      }
    }

    .auth-buttons {
      display: flex;
      gap: 8px;
    }

    @media (max-width: 576px) {
      .user-name {
        display: none;
      }

      .logo span {
        display: none;
      }
    }
  `]
})
export class HeaderComponent {
  // Inject AuthService
  authService = inject(AuthService);

  /**
   * Logout the current user
   */
  logout(): void {
    this.authService.logout();
  }
}
