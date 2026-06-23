import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent) },
  {
    path: 'applications',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/applications/applications.component').then((m) => m.ApplicationsComponent),
  },
  {
    path: 'applications/:id',
    pathMatch: 'full',
    redirectTo: 'applications/:id/features',
  },
  {
    path: 'applications/:id/:tab',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/application-detail/application-detail.component').then(
        (m) => m.ApplicationDetailComponent
      ),
  },
  { path: '', pathMatch: 'full', redirectTo: 'applications' },
  { path: '**', redirectTo: 'applications' },
];
