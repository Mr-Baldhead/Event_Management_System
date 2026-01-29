import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take } from 'rxjs/operators';

// Guard for authenticated routes
export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.currentUser$.pipe(
        take(1),
        map(user => {
            if (user) {
                // Check if must change password
                if (user.mustChangePassword && state.url !== '/change-password') {
                    router.navigate(['/change-password']);
                    return false;
                }
                return true;
            }

            // Not authenticated, redirect to login
            router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
            return false;
        })
    );
};

// Guard for non-authenticated routes (login page)
export const noAuthGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.currentUser$.pipe(
        take(1),
        map(user => {
            if (!user) {
                return true;
            }

            // Already authenticated, redirect based on role
            if (user.role === 'SUPERADMIN') {
                router.navigate(['/admin/users']);
            } else {
                router.navigate(['/events']);
            }
            return false;
        })
    );
};

// Guard for SuperAdmin only routes
export const superAdminGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.currentUser$.pipe(
        take(1),
        map(user => {
            if (user?.role === 'SUPERADMIN') {
                return true;
            }

            // Not SuperAdmin, redirect
            if (user) {
                router.navigate(['/events']);
            } else {
                router.navigate(['/login']);
            }
            return false;
        })
    );
};

// Guard for Admin only routes (regular admins and superadmins)
export const adminGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.currentUser$.pipe(
        take(1),
        map(user => {
            if (user && (user.role === 'ADMIN' || user.role === 'SUPERADMIN')) {
                return true;
            }

            // Not authenticated or not admin
            router.navigate(['/login']);
            return false;
        })
    );
};
