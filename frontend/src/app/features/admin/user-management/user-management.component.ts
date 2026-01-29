import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UserService, CreateUserRequest } from '../../../core/services/user.service';
import { AuthService, User } from '../../../core/services/auth.service';
import { CreateUserDialogComponent } from './create-user-dialog/create-user-dialog.component';

@Component({
    selector: 'app-user-management',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatTableModule,
        MatDialogModule,
        MatSnackBarModule,
        MatProgressSpinnerModule,
        MatTooltipModule
    ],
    template: `
        <div class="user-management-container">
            <!-- Header -->
            <div class="page-header">
                <div class="header-left">
                    <div class="header-icon">
                        <mat-icon>shield</mat-icon>
                    </div>
                    <div class="header-text">
                        <h1>Användare</h1>
                        <p class="subtitle">
                            Alla ({{ totalCount() }}) |
                            <span class="admin-link">Administratör ({{ adminCount() }})</span>
                        </p>
                    </div>
                </div>
                <button mat-flat-button class="add-button" (click)="openCreateDialog()">
                    <mat-icon>add</mat-icon>
                    Lägg till
                </button>
            </div>

            <!-- Loading state -->
            @if (loading()) {
                <div class="loading-container">
                    <mat-spinner diameter="48"></mat-spinner>
                </div>
            }

            <!-- User table -->
            @if (!loading()) {
                <mat-card class="users-card">
                    <table class="users-table">
                        <thead>
                        <tr>
                            <th>Användarnamn</th>
                            <th>Namn</th>
                            <th>E-postadress</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                            @for (user of users(); track user.id) {
                                <tr>
                                    <td>
                                        <div class="user-cell">
                                            <div class="user-avatar">
                                                {{ user.initials }}
                                            </div>
                                            <span class="username">{{ user.email }}</span>
                                        </div>
                                    </td>
                                    <td>{{ user.fullName }}</td>
                                    <td class="email-cell">{{ user.email }}</td>
                                    <td class="actions-cell">
                                        <button mat-icon-button
                                                (click)="deleteUser(user)"
                                                matTooltip="Ta bort användare"
                                                class="delete-button">
                                            <mat-icon>delete_outline</mat-icon>
                                        </button>
                                    </td>
                                </tr>
                            }
                        </tbody>
                    </table>

                    <div class="table-footer">
                        {{ users().length }} objekt
                    </div>
                </mat-card>
            }
        </div>
    `,
    styles: [`
      .user-management-container {
        min-height: 100vh;
        background: #fafafa;
        padding: 24px;
      }

      .page-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 24px;
      }

      .header-left {
        display: flex;
        align-items: flex-start;
        gap: 16px;
      }

      .header-icon {
        width: 56px;
        height: 56px;
        background: #8BC34A;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;

        mat-icon {
          color: white;
          font-size: 28px;
          width: 28px;
          height: 28px;
        }
      }

      .header-text {
        h1 {
          margin: 0;
          font-size: 20px;
          font-weight: 500;
          color: #333;
        }

        .subtitle {
          margin: 4px 0 0;
          font-size: 14px;
          color: #666;

          .admin-link {
            color: #8BC34A;
            cursor: pointer;
          }
        }
      }

      .add-button {
        background: #8BC34A !important;
        color: white !important;
        font-weight: 500;
        padding: 0 24px;
        height: 40px;

        mat-icon {
          margin-right: 4px;
        }
      }

      .loading-container {
        display: flex;
        justify-content: center;
        padding: 64px;
      }

      .users-card {
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        overflow: hidden;
      }

      .users-table {
        width: 100%;
        border-collapse: collapse;

        thead {
          background: #f5f5f5;

          th {
            padding: 16px 24px;
            text-align: left;
            font-weight: 500;
            color: #666;
            font-size: 14px;
            border-bottom: 1px solid #e0e0e0;
          }
        }

        tbody {
          tr {
            border-bottom: 1px solid #f0f0f0;
            transition: background 0.2s;

            &:hover {
              background: #fafafa;
            }

            &:last-child {
              border-bottom: none;
            }
          }

          td {
            padding: 16px 24px;
            font-size: 14px;
            color: #333;
          }
        }
      }

      .user-cell {
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .user-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #e8e8e8;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: #666;
        font-weight: 500;
      }

      .username {
        color: #8BC34A;
      }

      .email-cell {
        color: #8BC34A;
      }

      .actions-cell {
        text-align: right;
      }

      .delete-button {
        color: #999;

        &:hover {
          color: #f44336;
        }
      }

      .table-footer {
        padding: 12px 24px;
        text-align: right;
        font-size: 13px;
        color: #999;
        border-top: 1px solid #f0f0f0;
        background: #fafafa;
      }
    `]
})
export class UserManagementComponent implements OnInit {
    private userService = inject(UserService);
    private authService = inject(AuthService);
    private dialog = inject(MatDialog);
    private snackBar = inject(MatSnackBar);

    users = this.userService.users;
    loading = this.userService.loading;

    totalCount = signal(0);
    adminCount = signal(0);

    ngOnInit(): void {
        this.loadUsers();
        this.loadCounts();
    }

    private loadUsers(): void {
        this.userService.getAdmins().subscribe({
            error: (err) => {
                this.snackBar.open('Kunde inte hämta användare', 'Stäng', {
                    duration: 5000,
                    panelClass: 'error-snackbar'
                });
            }
        });
    }

    private loadCounts(): void {
        this.userService.getCounts().subscribe({
            next: (counts) => {
                this.totalCount.set(counts.total);
                this.adminCount.set(counts.admins);
            }
        });
    }

    openCreateDialog(): void {
        const dialogRef = this.dialog.open(CreateUserDialogComponent, {
            width: '500px',
            disableClose: true
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.loadCounts();
                this.snackBar.open(
                    `Administratör ${result.firstName || ''} ${result.lastName || ''} har lagts till`,
                    'Stäng',
                    { duration: 4000, panelClass: 'success-snackbar' }
                );
            }
        });
    }

    deleteUser(user: User): void {
        if (confirm(`Är du säker på att du vill ta bort ${user.fullName}?`)) {
            this.userService.deleteUser(user.id).subscribe({
                next: () => {
                    this.loadCounts();
                    this.snackBar.open(
                        `Administratör ${user.fullName} har tagits bort`,
                        'Stäng',
                        { duration: 4000, panelClass: 'success-snackbar' }
                    );
                },
                error: (err) => {
                    this.snackBar.open(
                        err.error?.error || 'Kunde inte ta bort användare',
                        'Stäng',
                        { duration: 5000, panelClass: 'error-snackbar' }
                    );
                }
            });
        }
    }
}