import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService, RentalService } from '@core/services';
import { VEHICLE_CATEGORIES } from '@core/models';

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './reservations.component.html',
  styleUrl: './reservations.component.scss'
})
export class ReservationsComponent implements OnInit {
  private authService = inject(AuthService);
  private rentalService = inject(RentalService);

  activeTab = signal<'active' | 'past'>('active');
  isLoading = signal(true);
  cancellingId = signal<number | null>(null);

  activeRentals = this.rentalService.activeRentals;
  pastRentals = this.rentalService.pastRentals;

  displayedRentals = computed(() => {
    return this.activeTab() === 'active'
      ? this.rentalService.activeRentals()
      : this.rentalService.pastRentals();
  });

  ngOnInit(): void {
    const userId = this.authService.currentUser()?.id;
    console.log('ReservationsComponent - currentUser:', this.authService.currentUser());
    console.log('ReservationsComponent - userId:', userId);

    if (!userId) {
      console.warn('ReservationsComponent - Pas de userId, utilisateur non connecté?');
      this.isLoading.set(false);
      return;
    }

    this.rentalService.getByUser(userId).subscribe({
      next: (rentals) => {
        console.log('ReservationsComponent - Rentals reçus:', rentals);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('ReservationsComponent - Erreur lors du chargement des réservations:', err);
        this.isLoading.set(false);
      }
    });
  }

  setTab(tab: 'active' | 'past'): void {
    this.activeTab.set(tab);
  }

  getCategoryName(code: string): string {
    return VEHICLE_CATEGORIES.find(c => c.code === code)?.name || code;
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'BOOKED': 'Réservée',
      'IN_PROGRESS': 'En cours',
      'COMPLETED': 'Terminée',
      'CANCELLED': 'Annulée'
    };
    return labels[status] || status;
  }

  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      'BOOKED': 'bg-blue-100 text-blue-700',
      'IN_PROGRESS': 'bg-green-100 text-green-700',
      'COMPLETED': 'bg-gray-100 text-gray-700',
      'CANCELLED': 'bg-red-100 text-red-700'
    };
    return classes[status] || 'bg-gray-100 text-gray-700';
  }

  canCancel(status: string): boolean {
    return status === 'BOOKED';
  }

  cancelRental(rentalId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir annuler cette réservation ?')) return;

    this.cancellingId.set(rentalId);

    this.rentalService.cancel(rentalId).subscribe({
      next: () => this.cancellingId.set(null),
      error: () => this.cancellingId.set(null)
    });
  }
}
