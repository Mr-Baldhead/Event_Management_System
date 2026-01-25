import { Routes } from '@angular/router';
import { authGuard, noAuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'events',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent),
    canActivate: [noAuthGuard]
  },
  {
    path: 'events',
    canActivate: [authGuard],
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
        path: ':id/allergy-report',
        loadComponent: () => import('./features/events/allergy-report/allergy-report.component')
          .then(m => m.AllergyReportComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'events'
  }
];
