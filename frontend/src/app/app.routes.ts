import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home.component')
      .then(m => m.HomeComponent)
  },
  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component')
          .then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component')
          .then(m => m.RegisterComponent)
      }
    ]
  },
  {
    path: 'search',
    loadComponent: () => import('./features/search/search.component')
      .then(m => m.SearchComponent)
  },
  {
    path: 'search/results',
    loadComponent: () => import('./features/search/results/results.component')
      .then(m => m.ResultsComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile.component')
      .then(m => m.ProfileComponent)
  },
  {
    path: 'reservations',
    canActivate: [authGuard],
    loadComponent: () => import('./features/reservations/reservations.component')
      .then(m => m.ReservationsComponent)
  },
  {
    path: 'payment/success',
    loadComponent: () => import('./features/payment/payment-success/payment-success.component')
      .then(m => m.PaymentSuccessComponent)
  },
  {
    path: 'payment/cancel',
    loadComponent: () => import('./features/payment/payment-cancel/payment-cancel.component')
      .then(m => m.PaymentCancelComponent)
  },
  {
    path: 'messages',
    canActivate: [authGuard],
    loadComponent: () => import('./features/messaging/messaging.component')
      .then(m => m.MessagingComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
