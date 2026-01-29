import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserService } from '../../../../core/services/user.service';

type PasswordStrength = 'svagt' | 'mellan' | 'starkt' | null;

@Component({
    selector: 'app-create-user-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatCheckboxModule,
        MatProgressSpinnerModule
    ],
    template: `
        <div class="dialog-container">
            <div class="dialog-header">
                <h2>Skapa ny administratör</h2>
                <p class="subtitle">Lägg till en ny administratör till systemet</p>
                <button mat-icon-button class="close-button" (click)="onCancel()">
                    <mat-icon>close</mat-icon>
                </button>
            </div>

            <form [formGroup]="form" (ngSubmit)="onSubmit()">
                <div class="form-content">
                    <!-- Name row -->
                    <div class="form-row">
                        <mat-form-field appearance="outline" class="half-width">
                            <mat-label>Förnamn</mat-label>
                            <input matInput formControlName="firstName" placeholder="Ange förnamn">
                        </mat-form-field>

                        <mat-form-field appearance="outline" class="half-width">
                            <mat-label>Efternamn</mat-label>
                            <input matInput formControlName="lastName" placeholder="Ange efternamn">
                        </mat-form-field>
                    </div>

                    <!-- Email -->
                    <mat-form-field appearance="outline" class="full-width">
                        <mat-label>E-postadress</mat-label>
                        <input matInput type="email" formControlName="email"
                               placeholder="namn&#64;exempel.se">
                        <span matSuffix class="required-star">*</span>
                        @if (form.get('email')?.hasError('required')) {
                            <mat-error>E-post är obligatoriskt</mat-error>
                        }
                        @if (form.get('email')?.hasError('email')) {
                            <mat-error>Ange en giltig e-postadress</mat-error>
                        }
                    </mat-form-field>

                    <!-- Password -->
                    <div class="password-section">
                        <label class="field-label">
                            Lösenord <span class="required-star">*</span>
                        </label>
                        <div class="password-row">
                            <mat-form-field appearance="outline" class="password-field">
                                <input matInput
                                       [type]="showPassword() ? 'text' : 'password'"
                                       formControlName="password"
                                       placeholder="Ange lösenord">
                                <button mat-icon-button matSuffix type="button"
                                        (click)="toggleShowPassword()">
                                    <mat-icon>{{ showPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                                </button>
                            </mat-form-field>
                            <button mat-stroked-button type="button" class="generate-button"
                                    (click)="generatePassword()">
                                <mat-icon>refresh</mat-icon>
                                Generera
                            </button>
                        </div>

                        <!-- Password strength indicator -->
                        @if (passwordStrength()) {
                            <div class="strength-indicator">
                                <span class="strength-label">Lösenordsstyrka:</span>
                                <span class="strength-badge" [ngClass]="passwordStrength()">
                                    {{ getStrengthLabel(passwordStrength()) }}
                                </span>
                            </div>
                        }

                        @if (form.get('password')?.touched && form.get('password')?.hasError('required')) {
                            <mat-error class="password-error">
                                Lösenord är obligatoriskt
                            </mat-error>
                        }
                        @if (form.get('password')?.touched && form.get('password')?.hasError('minlength')) {
                            <mat-error class="password-error">
                                Lösenordet måste vara minst 8 tecken
                            </mat-error>
                        }
                    </div>

                    <!-- Send notification checkbox -->
                    <div class="notification-section">
                        <mat-checkbox formControlName="sendNotification" color="primary">
                            <span class="checkbox-label">Skicka avisering till användaren via e-post</span>
                        </mat-checkbox>
                        <p class="checkbox-hint">
                            Användaren kommer att få ett välkomstmeddelande med inloggningsuppgifter
                        </p>
                    </div>
                </div>

                <!-- Submit button -->
                <div class="dialog-actions">
                    <button mat-flat-button type="submit" class="submit-button"
                            [disabled]="form.invalid || saving()">
                        @if (saving()) {
                            <mat-spinner diameter="20"></mat-spinner>
                        } @else {
                            Lägg till användare
                        }
                    </button>
                </div>
            </form>
        </div>
    `,
    styles: [`
        .dialog-container {
            padding: 24px;
        }

        .dialog-header {
            position: relative;
            margin-bottom: 24px;

            h2 {
                margin: 0;
                font-size: 20px;
                font-weight: 500;
                color: #333;
            }

            .subtitle {
                margin: 4px 0 0;
                font-size: 14px;
                color: #666;
            }

            .close-button {
                position: absolute;
                top: -8px;
                right: -8px;
                color: #999;
            }
        }

        .form-content {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .form-row {
            display: flex;
            gap: 16px;
        }

        .full-width {
            width: 100%;
        }

        .half-width {
            flex: 1;
        }

        .required-star {
            color: #f44336;
        }

        .field-label {
            display: block;
            font-size: 14px;
            color: #333;
            margin-bottom: 8px;
        }

        .password-section {
            margin-bottom: 16px;
        }

        .password-row {
            display: flex;
            gap: 12px;
        }

        .password-field {
            flex: 1;
        }

        .generate-button {
            height: 56px;
            white-space: nowrap;

            mat-icon {
                margin-right: 4px;
            }
        }

        .strength-indicator {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-top: 8px;
            padding: 12px;
            background: #f5f5f5;
            border-radius: 8px;
        }

        .strength-label {
            font-size: 13px;
            color: #666;
        }

        .strength-badge {
            padding: 4px 12px;
            border-radius: 16px;
            font-size: 12px;
            font-weight: 500;

            &.svagt {
                background: #ffebee;
                color: #c62828;
            }

            &.mellan {
                background: #fff8e1;
                color: #f57f17;
            }

            &.starkt {
                background: #e8f5e9;
                color: #2e7d32;
            }
        }

        .password-error {
            font-size: 12px;
            margin-top: 4px;
        }

        .notification-section {
            background: #f5f5e8;
            padding: 16px;
            border-radius: 8px;
            border: 1px solid #e8e8d8;

            .checkbox-label {
                font-size: 14px;
                color: #333;
            }

            .checkbox-hint {
                margin: 4px 0 0 32px;
                font-size: 13px;
                color: #666;
            }
        }

        .dialog-actions {
            margin-top: 24px;
        }

        .submit-button {
            width: 100%;
            height: 48px;
            background: #8BC34A !important;
            color: white !important;
            font-size: 15px;
            font-weight: 500;
            border-radius: 8px;
        }

        .submit-button:disabled {
            background: #ccc !important;
        }

        :host ::ng-deep .mat-mdc-form-field-flex {
            height: 56px !important;
        }

        :host ::ng-deep .mdc-text-field--outlined {
            --mdc-outlined-text-field-container-shape: 8px;
        }
    `]
})
export class CreateUserDialogComponent {
    private fb = inject(FormBuilder);
    private userService = inject(UserService);
    private dialogRef = inject(MatDialogRef<CreateUserDialogComponent>);

    form: FormGroup;
    showPassword = signal<boolean>(false);
    saving = signal<boolean>(false);
    passwordStrength = signal<PasswordStrength>(null);

    constructor() {
        this.form = this.fb.group({
            firstName: [''],
            lastName: [''],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(8)]],
            sendNotification: [false]
        });

        // Watch password changes
        this.form.get('password')?.valueChanges.subscribe(value => {
            this.passwordStrength.set(this.userService.calculatePasswordStrength(value));
        });
    }

    toggleShowPassword(): void {
        this.showPassword.set(!this.showPassword());
    }

    generatePassword(): void {
        const password = this.userService.generatePassword(16);
        this.form.patchValue({ password });
        this.showPassword.set(true);
    }

    getStrengthLabel(strength: PasswordStrength): string {
        switch (strength) {
            case 'svagt': return 'Svagt';
            case 'mellan': return 'Mellan';
            case 'starkt': return 'Starkt';
            default: return '';
        }
    }

    onSubmit(): void {
        if (this.form.invalid) return;

        this.saving.set(true);
        const formValue = this.form.value;

        this.userService.createAdmin({
            firstName: formValue.firstName || undefined,
            lastName: formValue.lastName || undefined,
            email: formValue.email,
            password: formValue.password,
            sendNotification: formValue.sendNotification
        }).subscribe({
            next: (user) => {
                this.saving.set(false);
                this.dialogRef.close(user);
            },
            error: () => {
                this.saving.set(false);
            }
        });
    }

    onCancel(): void {
        this.dialogRef.close();
    }
}
