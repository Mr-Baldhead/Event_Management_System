import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard to protect routes that require authentication.
 * Redirects to login page if user is not authenticated.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Store the attempted URL for redirecting after login
  const returnUrl = state.url;
  
  // Navigate to login with return URL
  router.navigate(['/login'], { 
    queryParams: { returnUrl } 
  });
  
  return false;
};

/**
 * Guard to protect routes that require admin role.
 * Redirects to home if user is not an admin.
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { 
      queryParams: { returnUrl: state.url } 
    });
    return false;
  }

  if (!authService.isAdmin()) {
    router.navigate(['/']);
    return false;
  }

  return true;
};

/**
 * Guard to protect routes that require superadmin role.
 */
export const superAdminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { 
      queryParams: { returnUrl: state.url } 
    });
    return false;
  }

  if (!authService.isSuperAdmin()) {
    router.navigate(['/']);
    return false;
  }

  return true;
};

/**
 * Guard to prevent authenticated users from accessing login page.
 * Redirects to home if user is already logged in.
 */
export const noAuthGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    router.navigate(['/']);
    return false;
  }

  return true;
};

// Alias for backwards compatibility
export const guestGuard = noAuthGuard;
