import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '@env/environment';
import { Offer, CheckoutSessionResponse, CreateCheckoutSessionRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly apiUrl = `${environment.apiUrl}/payments`;

  private isProcessingSignal = signal<boolean>(false);

  readonly isProcessing = this.isProcessingSignal.asReadonly();

  constructor(private http: HttpClient) {}

  createCheckoutSession(offer: Offer): Observable<CheckoutSessionResponse> {
    this.isProcessingSignal.set(true);

    const request: CreateCheckoutSessionRequest = {
      catCar: offer.catCar,
      startDate: offer.startDate,
      endDate: offer.endDate,
      price: offer.price,
      departureAgencyId: offer.departureAgency.id,
      returnAgencyId: offer.returnAgency.id,
      departureAgencyName: offer.departureAgency.name,
      returnAgencyName: offer.returnAgency.name,
      departureCity: offer.departureAgency.city,
      returnCity: offer.returnAgency.city
    };

    return this.http.post<CheckoutSessionResponse>(
      `${this.apiUrl}/create-checkout-session`,
      request
    ).pipe(
      tap({
        next: () => this.isProcessingSignal.set(false),
        error: () => this.isProcessingSignal.set(false)
      })
    );
  }

  redirectToCheckout(url: string): void {
    window.location.href = url;
  }
}
