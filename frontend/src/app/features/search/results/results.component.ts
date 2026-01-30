import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { OfferService, AuthService, PaymentService } from '@core/services';
import { Offer, VEHICLE_CATEGORIES } from '@core/models';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './results.component.html',
  styleUrl: './results.component.scss'
})
export class ResultsComponent implements OnInit {
  private router = inject(Router);
  private offerService = inject(OfferService);
  private authService = inject(AuthService);
  paymentService = inject(PaymentService);

  offers = this.offerService.offers;
  lastSearch = this.offerService.lastSearch;

  sortedOffers: Offer[] = [];
  sortOrder = 'price-asc';
  errorMessage = signal('');
  selectedOfferId = signal<number | null>(null);

  ngOnInit(): void {
    this.sortedOffers = [...this.offers()];
    this.sortOffers({ target: { value: 'price-asc' } } as any);

    if (this.offers().length === 0) {
      this.router.navigate(['/search']);
    }
  }

  getCategoryName(code: string): string {
    return VEHICLE_CATEGORIES.find(c => c.code === code)?.name || code;
  }

  sortOffers(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.sortOrder = value;

    this.sortedOffers = [...this.offers()].sort((a, b) => {
      if (value === 'price-asc') return a.price - b.price;
      return b.price - a.price;
    });
  }

  bookOffer(offer: Offer): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/auth/login']);
      return;
    }

    this.errorMessage.set('');
    this.selectedOfferId.set(offer.id);

    this.paymentService.createCheckoutSession(offer).subscribe({
      next: (response) => {
        this.paymentService.redirectToCheckout(response.url);
      },
      error: (err) => {
        this.selectedOfferId.set(null);
        this.errorMessage.set(err.error?.message || 'Erreur lors de la création du paiement. Veuillez réessayer.');
        console.error('Erreur paiement:', err);
      }
    });
  }
}
