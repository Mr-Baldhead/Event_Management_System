import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';

type PasswordStrength = 'svagt' | 'mellan' | 'starkt' | null;

@Component({
    selector: 'app-change-password',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatSnackBarModule
    ],
    template: `
        <div class="change-password-container">
            <mat-card class="change-password-card">
                <mat-card-content>
                    <h1 class="title">Byt lösenord</h1>
                    <p class="subtitle">
                        Du måste byta ditt lösenord innan du kan fortsätta.
                    </p>

                    <form [formGroup]="form" (ngSubmit)="onSubmit()">
                        <!-- Current password (only if not first login) -->
                        <mat-form-field *ngIf="!mustChangePassword()" appearance="outline" class="full-width">
                            <mat-label>Nuvarande lösenord</mat-label>
                            <input matInput 
                                   [type]="showCurrentPassword() ? 'text' : 'password'" 
                                   formControlName="currentPassword">
                            <button mat-icon-button matSuffix type="button"
                                    (click)="showCurrentPassword.set(!showCurrentPassword())">
                                <mat-icon>{{ showCurrentPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                            </button>
                            <mat-error>Nuvarande lösenord krävs</mat-error>
                        </mat-form-field>

                        <!-- New password -->
                        <mat-form-field appearance="outline" class="full-width">
                            <mat-label>Nytt lösenord</mat-label>
                            <input matInput 
                                   [type]="showNewPassword() ? 'text' : 'password'" 
                                   formControlName="newPassword">
                            <button mat-icon-button matSuffix type="button"
                                    (click)="showNewPassword.set(!showNewPassword())">
                                <mat-icon>{{ showNewPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                            </button>
                            <mat-error *ngIf="form.get('newPassword')?.hasError('required')">
                                Nytt lösenord krävs
                            </mat-error>
                            <mat-error *ngIf="form.get('newPassword')?.hasError('minlength')">
                                Lösenordet måste vara minst 8 tecken
                            </mat-error>
                        </mat-form-field>

                        <!-- Password strength indicator -->
                        <div *ngIf="passwordStrength()" class="strength-indicator">
                            <span class="strength-label">Lösenordsstyrka:</span>
                            <span class="strength-badge" [ngClass]="passwordStrength()">
                                {{ getStrengthLabel(passwordStrength()) }}
                            </span>
                        </div>

                        <!-- Confirm password -->
                        <mat-form-field appearance="outline" class="full-width">
                            <mat-label>Bekräfta nytt lösenord</mat-label>
                            <input matInput 
                                   [type]="showConfirmPassword() ? 'text' : 'password'" 
                                   formControlName="confirmPassword">
                            <button mat-icon-button matSuffix type="button"
                                    (click)="showConfirmPassword.set(!showConfirmPassword())">
                                <mat-icon>{{ showConfirmPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                            </button>
                            <mat-error *ngIf="form.get('confirmPassword')?.hasError('required')">
                                Bekräfta lösenordet
                            </mat-error>
                        </mat-form-field>

                        <!-- Password mismatch error -->
                        <div *ngIf="passwordMismatch()" class="error-message">
                            <mat-icon>error</mat-icon>
                            Lösenorden matchar inte
                        </div>

                        <button mat-flat-button type="submit" class="submit-button"
                                [disabled]="form.invalid || passwordMismatch() || saving()">
                            @if (saving()) {
                                <mat-spinner diameter="20"></mat-spinner>
                            } @else {
                                Byt lösenord
                            }
                        </button>
                    </form>
                </mat-card-content>
            </mat-card>
        </div>
    `,
    styles: [`
        .change-password-container {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #416487;
            padding: 16px;
        }

        .change-password-card {
            width: 100%;
            max-width: 440px;
            padding: 40px;
            border-radius: 16px;
        }

        .title {
            text-align: center;
            font-size: 22px;
            font-weight: 500;
            color: #333;
            margin: 0 0 8px 0;
        }

        .subtitle {
            text-align: center;
            font-size: 14px;
            color: #666;
            margin: 0 0 32px 0;
        }

        .full-width {
            width: 100%;
            margin-bottom: 16px;
        }

        .strength-indicator {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 16px;
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

        .error-message {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 12px;
            background: #ffebee;
            color: #c62828;
            border-radius: 8px;
            margin-bottom: 16px;
            font-size: 14px;

            mat-icon {
                font-size: 20px;
                width: 20px;
                height: 20px;
            }
        }

        .submit-button {
            width: 100%;
            height: 48px;
            font-size: 16px;
            background-color: #416487 !important;
            color: white !important;
            border-radius: 8px;
            margin-top: 8px;
        }

        .submit-button:disabled {
            background-color: #ccc !important;
        }

        :host ::ng-deep .mat-mdc-form-field-flex {
            height: 56px !important;
        }

        :host ::ng-deep .mdc-text-field--outlined {
            --mdc-outlined-text-field-container-shape: 8px;
        }
    `]
})
export class ChangePasswordComponent {
    private fb = inject(FormBuilder);
    private router = inject(Router);
    private authService = inject(AuthService);
    private userService = inject(UserService);
    private snackBar = inject(MatSnackBar);

    form: FormGroup;
    saving = signal(false);
    showCurrentPassword = signal(false);
    showNewPassword = signal(false);
    showConfirmPassword = signal(false);
    passwordStrength = signal<PasswordStrength>(null);
    mustChangePassword = signal(false);

    constructor() {
        // Check if this is first login (must change password)
        const user = this.authService.getCurrentUser();
        this.mustChangePassword.set(user?.mustChangePassword ?? false);

        this.form = this.fb.group({
            currentPassword: [this.mustChangePassword() ? '' : '', 
                this.mustChangePassword() ? [] : [Validators.required]],
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', Validators.required]
        });

        // Watch password changes
        this.form.get('newPassword')?.valueChanges.subscribe(value => {
            this.passwordStrength.set(this.userService.calculatePasswordStrength(value));
        });
    }

    passwordMismatch(): boolean {
        const newPassword = this.form.get('newPassword')?.value;
        const confirmPassword = this.form.get('confirmPassword')?.value;
        return confirmPassword && newPassword !== confirmPassword;
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
        if (this.form.invalid || this.passwordMismatch()) return;

        this.saving.set(true);
        const { currentPassword, newPassword } = this.form.value;

        this.authService.changePassword(
            this.mustChangePassword() ? null : currentPassword,
            newPassword
        ).subscribe({
            next: () => {
                this.saving.set(false);
                this.snackBar.open('Lösenordet har bytts!', 'OK', { duration: 3000 });

                // Navigate based on role
                const user = this.authService.getCurrentUser();
                if (user?.role === 'SUPERADMIN') {
                    this.router.navigate(['/admin/users']);
                } else {
                    this.router.navigate(['/events']);
                }
            },
            error: (err) => {
                this.saving.set(false);
                this.snackBar.open(
                    err.error?.error || 'Kunde inte byta lösenord',
                    'Stäng',
                    { duration: 5000, panelClass: 'error-snackbar' }
                );
            }
        });
    }
}
