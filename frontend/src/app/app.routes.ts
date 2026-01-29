import { Routes } from '@angular/router';
import { authGuard, noAuthGuard, superAdminGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
    },
    {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component')
            .then(m => m.LoginComponent),
        canActivate: [noAuthGuard]
    },
    {
        path: 'change-password',
        loadComponent: () => import('./features/auth/change-password/change-password.component')
            .then(m => m.ChangePasswordComponent),
        canActivate: [authGuard]
    },
    {
        path: 'admin',
        canActivate: [superAdminGuard],
        children: [
            {
                path: '',
                redirectTo: 'users',
                pathMatch: 'full'
            },
            {
                path: 'users',
                loadComponent: () => import('./features/admin/user-management/user-management.component')
                    .then(m => m.UserManagementComponent)
            }
        ]
    },
    {
        path: 'events',
        canActivate: [adminGuard],
        children: [
            {
                path: '',
                loadComponent: () => import('./features/events/event-list/event-list.component')
                    .then(m => m.EventListComponent)
            },
            {
                path: 'new',
                loadComponent: () => import('./features/events/event-form/event-form.component')
                    .then(m => m.EventFormComponent)
            },
            {
                path: ':id',
                loadComponent: () => import('./features/events/event-detail/event-detail.component')
                    .then(m => m.EventDetailComponent)
            },
            {
                path: ':id/edit',
                loadComponent: () => import('./features/events/event-form/event-form.component')
                    .then(m => m.EventFormComponent)
            },
            {
                path: ':id/form-builder',
                loadComponent: () => import('./features/events/form-builder/form-builder.component')
                    .then(m => m.FormBuilderComponent)
            },
            {
                path: ':id/allergy-report',
                loadComponent: () => import('./features/events/allergy-report/allergy-report.component')
                    .then(m => m.AllergyReportComponent)
            }
        ]
    },
    {
        path: '**',
        redirectTo: 'login'
    }
];
