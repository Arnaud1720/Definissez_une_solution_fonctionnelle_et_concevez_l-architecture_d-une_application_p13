import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '@env/environment';
import { Offer, SearchOfferRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class OfferService {
  private readonly apiUrl = `${environment.apiUrl}/offers`;

  private offersSignal = signal<Offer[]>([]);
  private lastSearchSignal = signal<SearchOfferRequest | null>(null);

  readonly offers = this.offersSignal.asReadonly();
  readonly lastSearch = this.lastSearchSignal.asReadonly();

  constructor(private http: HttpClient) {}

  search(criteria: SearchOfferRequest): Observable<Offer[]> {
    this.lastSearchSignal.set(criteria);

    return this.http.post<Offer[]>(`${this.apiUrl}/search`, criteria).pipe(
      tap(offers => this.offersSignal.set(offers))
    );
  }

  clearOffers(): void {
    this.offersSignal.set([]);
    this.lastSearchSignal.set(null);
  }
}
