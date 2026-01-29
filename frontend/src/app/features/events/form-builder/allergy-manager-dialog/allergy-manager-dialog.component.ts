import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FoodAllergyService } from '../../../../core/services/food-allergy.service';
import { FoodAllergy } from '../../../../shared/models/form-field.model';

// Dialog for managing global food allergies
@Component({
    selector: 'app-allergy-manager-dialog',
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
                <h2>Matallergier</h2>
                <button mat-icon-button (click)="close()">
                    <mat-icon>close</mat-icon>
                </button>
            </div>

            <div class="dialog-content">
                <!-- Allergy list -->
                <div class="allergy-grid">
                    @for (allergy of allergyService.allergies(); track allergy.id) {
                        <div class="allergy-item">
                            <button
                                mat-icon-button
                                class="delete-btn"
                                (click)="deleteAllergy(allergy)"
                                matTooltip="Ta bort">
                                <mat-icon>delete</mat-icon>
                            </button>
                            <span class="allergy-name">{{ allergy.name }}</span>
                        </div>
                    }
                </div>

                <!-- Add new allergy -->
                <div class="add-section">
                    <mat-form-field appearance="outline" class="add-input">
                        <mat-label>Lägg till Matallergi</mat-label>
                        <input
                            matInput
                            [(ngModel)]="newAllergyName"
                            (keyup.enter)="addAllergy()"
                            placeholder="Matallergi">
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
                    (click)="addAllergy()"
                    [disabled]="!newAllergyName.trim()">
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

        .allergy-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 8px;
            margin-bottom: 24px;
        }

        .allergy-item {
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

            .allergy-name {
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
export class AllergyManagerDialogComponent {
    private dialogRef = inject(MatDialogRef<AllergyManagerDialogComponent>);
    private snackBar = inject(MatSnackBar);
    allergyService = inject(FoodAllergyService);

    newAllergyName = '';

    addAllergy(): void {
        const name = this.newAllergyName.trim();
        if (!name) return;

        this.allergyService.createAllergy(name).subscribe({
            next: () => {
                this.newAllergyName = '';
                this.snackBar.open('Matallergi tillagd', 'Stäng', { duration: 2000 });
            },
            error: (err) => {
                if (err.status === 409) {
                    this.snackBar.open('Denna matallergi finns redan', 'Stäng', { duration: 3000 });
                } else {
                    this.snackBar.open('Kunde inte lägga till matallergi', 'Stäng', { duration: 3000 });
                }
            }
        });
    }

    deleteAllergy(allergy: FoodAllergy): void {
        if (!allergy.id) return;

        this.allergyService.deleteAllergy(allergy.id).subscribe({
            next: () => {
                this.snackBar.open('Matallergi borttagen', 'Stäng', { duration: 2000 });
            },
            error: () => {
                this.snackBar.open('Kunde inte ta bort matallergi', 'Stäng', { duration: 3000 });
            }
        });
    }

    close(): void {
        this.dialogRef.close();
    }
}
