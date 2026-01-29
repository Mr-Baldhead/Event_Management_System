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

@Component({
    selector: 'app-login',
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
        <div class="login-container">
            <mat-card class="login-card">
                <mat-card-content>
                    <h1 class="login-title">Logga in för att fortsätta</h1>

                    <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
                        <mat-form-field appearance="outline" class="full-width">
                            <mat-label>E-post</mat-label>
                            <input matInput type="email" formControlName="email" 
                                   placeholder="namn@exempel.se">
                            <mat-error *ngIf="loginForm.get('email')?.hasError('required')">
                                E-post krävs
                            </mat-error>
                            <mat-error *ngIf="loginForm.get('email')?.hasError('email')">
                                Ange en giltig e-postadress
                            </mat-error>
                        </mat-form-field>

                        <mat-form-field appearance="outline" class="full-width">
                            <mat-label>Lösenord</mat-label>
                            <input matInput [type]="showPassword() ? 'text' : 'password'" 
                                   formControlName="password" placeholder="Ange lösenord">
                            <button mat-icon-button matSuffix type="button"
                                    (click)="showPassword.set(!showPassword())">
                                <mat-icon>{{ showPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                            </button>
                            <mat-error *ngIf="loginForm.get('password')?.hasError('required')">
                                Lösenord krävs
                            </mat-error>
                        </mat-form-field>

                        <button mat-flat-button type="submit" class="login-button"
                                [disabled]="loginForm.invalid || loading()">
                            @if (loading()) {
                                <mat-spinner diameter="20"></mat-spinner>
                            } @else {
                                Logga in
                            }
                        </button>
                    </form>

                    <a class="forgot-password-link" (click)="onForgotPassword()">
                        Glömt lösenordet
                    </a>
                </mat-card-content>
            </mat-card>
        </div>
    `,
    styles: [`
        .login-container {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #416487;
            padding: 16px;
        }

        .login-card {
            width: 100%;
            max-width: 400px;
            padding: 48px 40px;
            border-radius: 16px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
        }

        .login-title {
            text-align: center;
            font-size: 18px;
            font-weight: 500;
            color: #666;
            margin: 0 0 32px 0;
        }

        .full-width {
            width: 100%;
            margin-bottom: 16px;
        }

        :host ::ng-deep .mat-mdc-form-field-flex {
            height: 56px !important;
        }

        :host ::ng-deep .mat-mdc-text-field-wrapper {
            background: #fafafa;
        }

        :host ::ng-deep .mdc-text-field--outlined {
            --mdc-outlined-text-field-container-shape: 8px;
        }

        .login-button {
            width: 100%;
            height: 48px;
            font-size: 16px;
            background-color: #e0e0e0 !important;
            color: #666 !important;
            border-radius: 8px;
            margin-top: 8px;
        }

        .login-button:not(:disabled):hover {
            background-color: #416487 !important;
            color: white !important;
        }

        .login-button:disabled {
            background-color: #f5f5f5 !important;
        }

        .forgot-password-link {
            display: block;
            text-align: center;
            margin-top: 24px;
            color: #666;
            font-size: 14px;
            cursor: pointer;
            text-decoration: none;
        }

        .forgot-password-link:hover {
            text-decoration: underline;
            color: #416487;
        }

        mat-spinner {
            display: inline-block;
        }

        ::ng-deep .mat-mdc-progress-spinner circle {
            stroke: #666 !important;
        }
    `]
})
export class LoginComponent {
    private fb = inject(FormBuilder);
    private router = inject(Router);
    private authService = inject(AuthService);
    private snackBar = inject(MatSnackBar);

    loginForm: FormGroup;
    loading = signal(false);
    showPassword = signal(false);

    constructor() {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', Validators.required]
        });
    }

    onSubmit(): void {
        if (this.loginForm.invalid) return;

        this.loading.set(true);
        const { email, password } = this.loginForm.value;

        this.authService.login(email, password).subscribe({
            next: (response) => {
                this.loading.set(false);

                if (response.mustChangePassword) {
                    this.router.navigate(['/change-password']);
                } else if (response.user.role === 'SUPERADMIN') {
                    this.router.navigate(['/admin/users']);
                } else {
                    this.router.navigate(['/events']);
                }
            },
            error: (err) => {
                this.loading.set(false);
                this.snackBar.open(
                    err.error?.error || 'Inloggningen misslyckades',
                    'Stäng',
                    { duration: 5000, panelClass: 'error-snackbar' }
                );
            }
        });
    }

    onForgotPassword(): void {
        this.snackBar.open(
            'Kontakta administratören för att återställa ditt lösenord',
            'OK',
            { duration: 5000 }
        );
    }
}
