import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';

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
        <mat-toolbar class="header">
            <a routerLink="/" class="logo">
                <mat-icon>event</mat-icon>
                <span>Event Manager</span>
            </a>

            <span class="spacer"></span>

            @if (authService.isLoggedIn()) {
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
                    <button mat-menu-item (click)="onLogout()">
                        <mat-icon>logout</mat-icon>
                        <span>Logga ut</span>
                    </button>
                </mat-menu>
            }
        </mat-toolbar>
    `,
    styles: [`
      .header {
        background: white;
        border-bottom: 1px solid #e0e0e0;
        position: sticky;
        top: 0;
        z-index: 100;
      }

      .logo {
        display: flex;
        align-items: center;
        gap: 8px;
        text-decoration: none;
        color: #1D968C;
        font-weight: 500;
        font-size: 18px;

        mat-icon {
          font-size: 28px;
          width: 28px;
          height: 28px;
        }
      }

      .spacer {
        flex: 1;
      }

      .user-button {
        display: flex;
        align-items: center;
        gap: 4px;
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
        gap: 2px;
      }

      .menu-name {
        font-weight: 500;
        color: #333;
      }

      .menu-role {
        font-size: 12px;
        color: #666;
        text-transform: capitalize;
      }
    `]
})
export class HeaderComponent {
    authService = inject(AuthService);

    onLogout(): void {
        this.authService.logout();
    }
}