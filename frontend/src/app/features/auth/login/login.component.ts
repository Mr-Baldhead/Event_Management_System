import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';

/**
 * Login component for user authentication.
 * Provides email/password form with validation.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon class="login-icon">event</mat-icon>
            <span>Event Manager</span>
          </mat-card-title>
          <mat-card-subtitle>Logga in för att fortsätta</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <!-- Error message -->
          @if (authService.error()) {
            <div class="error-message">
              <mat-icon>error</mat-icon>
              {{ authService.error() }}
            </div>
          }

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <!-- Email field -->
            <mat-form-field appearance="outline">
              <mat-label>E-post</mat-label>
              <input 
                matInput 
                type="email" 
                formControlName="email"
                placeholder="din@email.se"
                autocomplete="email">
              <mat-icon matPrefix>email</mat-icon>
              @if (loginForm.get('email')?.hasError('required') && loginForm.get('email')?.touched) {
                <mat-error>E-post krävs</mat-error>
              }
              @if (loginForm.get('email')?.hasError('email') && loginForm.get('email')?.touched) {
                <mat-error>Ogiltig e-postadress</mat-error>
              }
            </mat-form-field>

            <!-- Password field -->
            <mat-form-field appearance="outline">
              <mat-label>Lösenord</mat-label>
              <input 
                matInput 
                [type]="hidePassword ? 'password' : 'text'" 
                formControlName="password"
                autocomplete="current-password">
              <mat-icon matPrefix>lock</mat-icon>
              <button 
                mat-icon-button 
                matSuffix 
                type="button"
                (click)="hidePassword = !hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (loginForm.get('password')?.hasError('required') && loginForm.get('password')?.touched) {
                <mat-error>Lösenord krävs</mat-error>
              }
              @if (loginForm.get('password')?.hasError('minlength') && loginForm.get('password')?.touched) {
                <mat-error>Lösenord måste vara minst 6 tecken</mat-error>
              }
            </mat-form-field>

            <!-- Submit button -->
            <button 
              mat-flat-button 
              color="primary" 
              type="submit"
              class="submit-button"
              [disabled]="loginForm.invalid || authService.isLoading()">
              @if (authService.isLoading()) {
                <mat-spinner diameter="20"></mat-spinner>
                <span>Loggar in...</span>
              } @else {
                <mat-icon>login</mat-icon>
                <span>Logga in</span>
              }
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <a routerLink="/forgot-password" class="forgot-link">Glömt lösenord?</a>
        </mat-card-actions>
      </mat-card>

      <!-- Dev login buttons (remove in production) -->
      <div class="dev-buttons">
        <p>Utvecklingsläge:</p>
        <button mat-stroked-button (click)="devLogin('ADMIN')">
          <mat-icon>person</mat-icon>
          Dev Admin
        </button>
        <button mat-stroked-button (click)="devLogin('SUPERADMIN')">
          <mat-icon>admin_panel_settings</mat-icon>
          Dev SuperAdmin
        </button>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: linear-gradient(135deg, #1D968C 0%, #136E67 100%);
    }

    .login-card {
      width: 100%;
      max-width: 400px;
      padding: 24px;
    }

    mat-card-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      margin-bottom: 24px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 24px;
      color: #1D968C;

      .login-icon {
        font-size: 32px;
        width: 32px;
        height: 32px;
      }
    }

    mat-card-subtitle {
      margin-top: 8px;
    }

    .error-message {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      margin-bottom: 16px;
      background: rgba(220, 53, 69, 0.1);
      border: 1px solid rgba(220, 53, 69, 0.3);
      border-radius: 8px;
      color: #dc3545;

      mat-icon {
        font-size: 20px;
        width: 20px;
        height: 20px;
      }
    }

    form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    mat-form-field {
      width: 100%;
    }

    .submit-button {
      height: 48px;
      font-size: 16px;
      margin-top: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;

      mat-spinner {
        margin-right: 8px;
      }
    }

    mat-card-actions {
      display: flex;
      justify-content: center;
      padding: 16px 0 0 0;
    }

    .forgot-link {
      color: #1D968C;
      text-decoration: none;
      font-size: 14px;

      &:hover {
        text-decoration: underline;
      }
    }

    .dev-buttons {
      margin-top: 24px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      p {
        color: rgba(255, 255, 255, 0.7);
        font-size: 12px;
        margin: 0;
      }

      button {
        background: rgba(255, 255, 255, 0.9);
      }
    }
  `]
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  
  authService = inject(AuthService);
  
  loginForm!: FormGroup;
  hidePassword = true;
  private returnUrl = '/';

  ngOnInit(): void {
    // Clear any previous errors
    this.authService.clearError();
    
    // Get return URL from route parameters
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    
    // Initialize form
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      const { email, password } = this.loginForm.value;
      
      this.authService.login({ email, password }).subscribe(response => {
        if (response) {
          this.router.navigateByUrl(this.returnUrl);
        }
      });
    }
  }

  /**
   * Development login - bypasses backend
   */
  devLogin(role: 'ADMIN' | 'SUPERADMIN'): void {
    this.authService.devLogin(role);
    this.router.navigateByUrl(this.returnUrl);
  }
}
