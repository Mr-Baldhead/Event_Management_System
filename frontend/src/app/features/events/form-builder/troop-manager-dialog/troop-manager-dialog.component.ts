import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TroopService } from '../../../../core/services/troop.service';
import { Troop } from '../../../../shared/models/form-field.model';

// Dialog for managing global troops
@Component({
    selector: 'app-troop-manager-dialog',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSnackBarModule,
        MatTooltipModule
    ],
    template: `
        <div class="dialog-container">
            <div class="dialog-header">
                <h2>Kårer</h2>
                <button mat-icon-button (click)="close()">
                    <mat-icon>close</mat-icon>
                </button>
            </div>

            <div class="dialog-content">
                <!-- Troop list -->
                <div class="troop-grid">
                    @for (troop of troopService.troops(); track troop.id) {
                        <div class="troop-item">
                            <button
                                mat-icon-button
                                class="delete-btn"
                                (click)="deleteTroop(troop)"
                                matTooltip="Ta bort">
                                <mat-icon>delete</mat-icon>
                            </button>
                            <span class="troop-name">{{ troop.name }}</span>
                        </div>
                    }
                </div>

                <!-- Add new troop -->
                <div class="add-section">
                    <mat-form-field appearance="outline" class="add-input">
                        <mat-label>Lägg till en ny kår</mat-label>
                        <input
                            matInput
                            [(ngModel)]="newTroopName"
                            (keyup.enter)="addTroop()"
                            placeholder="Kårnamn">
                    </mat-form-field>
                </div>
            </div>

            <div class="dialog-actions">
                <button mat-stroked-button (click)="close()">
                    <mat-icon>close</mat-icon>
                    Avbryt
                </button>
                <button
                    mat-flat-button
                    color="primary"
                    (click)="addTroop()"
                    [disabled]="!newTroopName.trim()">
                    <mat-icon>add</mat-icon>
                    Lägg till
                </button>
            </div>
        </div>
    `,
    styles: [`
        .dialog-container {
            min-width: 600px;
        }

        .dialog-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px 24px;
            border-bottom: 1px solid #e0e0e0;

            h2 {
                margin: 0;
                font-size: 24px;
                font-weight: 600;
            }
        }

        .dialog-content {
            padding: 24px;
            max-height: 400px;
            overflow-y: auto;
        }

        .troop-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 8px;
            margin-bottom: 24px;
        }

        .troop-item {
            display: flex;
            align-items: center;
            gap: 4px;
            padding: 4px;
            border-radius: 4px;

            &:hover {
                background: #f5f5f5;

                .delete-btn {
                    opacity: 1;
                }
            }

            .delete-btn {
                width: 28px;
                height: 28px;
                opacity: 0.5;

                mat-icon {
                    font-size: 18px;
                    width: 18px;
                    height: 18px;
                }

                &:hover {
                    opacity: 1;
                }
            }

            .troop-name {
                font-size: 14px;
                color: #333;
            }
        }

        .add-section {
            display: flex;
            gap: 12px;
            align-items: flex-start;

            .add-input {
                flex: 1;
            }
        }

        .dialog-actions {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            padding: 16px 24px;
            border-top: 1px solid #e0e0e0;

            button {
                display: flex;
                align-items: center;
                gap: 4px;
            }

            button[color="primary"] {
                background-color: #c45c4b;

                &:hover {
                    background-color: #b04a3a;
                }
            }
        }
    `]
})
export class TroopManagerDialogComponent {
    private dialogRef = inject(MatDialogRef<TroopManagerDialogComponent>);
    private snackBar = inject(MatSnackBar);
    troopService = inject(TroopService);

    newTroopName = '';

    addTroop(): void {
        const name = this.newTroopName.trim();
        if (!name) return;

        this.troopService.createTroop(name).subscribe({
            next: () => {
                this.newTroopName = '';
                this.snackBar.open('Kår tillagd', 'Stäng', { duration: 2000 });
            },
            error: (err) => {
                if (err.status === 409) {
                    this.snackBar.open('En kår med detta namn finns redan', 'Stäng', { duration: 3000 });
                } else {
                    this.snackBar.open('Kunde inte lägga till kår', 'Stäng', { duration: 3000 });
                }
            }
        });
    }

    deleteTroop(troop: Troop): void {
        if (!troop.id) return;

        this.troopService.deleteTroop(troop.id).subscribe({
            next: () => {
                this.snackBar.open('Kår borttagen', 'Stäng', { duration: 2000 });
            },
            error: () => {
                this.snackBar.open('Kunde inte ta bort kår', 'Stäng', { duration: 3000 });
            }
        });
    }

    close(): void {
        this.dialogRef.close();
    }
}
