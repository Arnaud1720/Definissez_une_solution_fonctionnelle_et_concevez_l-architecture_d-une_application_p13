import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '@env/environment';
import { Rental, CreateRentalRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class RentalService {
  private readonly apiUrl = `${environment.apiUrl}/rantals`;

  private rentalsSignal = signal<Rental[]>([]);

  readonly rentals = this.rentalsSignal.asReadonly();

  readonly activeRentals = computed(() =>
    this.rentalsSignal().filter(r =>
      r.status === 'BOOKED' || r.status === 'IN_PROGRESS'
    )
  );

  readonly pastRentals = computed(() =>
    this.rentalsSignal().filter(r =>
      r.status === 'COMPLETED' || r.status === 'CANCELLED'
    )
  );

  constructor(private http: HttpClient) {}

  getByUser(userId: number): Observable<Rental[]> {
    const url = `${this.apiUrl}/user/${userId}`;
    console.log('RentalService.getByUser - URL:', url);
    return this.http.get<Rental[]>(url).pipe(
      tap(rentals => {
        console.log('RentalService.getByUser - Réponse:', rentals);
        this.rentalsSignal.set(rentals);
      })
    );
  }

  create(rental: CreateRentalRequest): Observable<Rental> {
    return this.http.post<Rental>(`${this.apiUrl}/save`, rental).pipe(
      tap(newRental => {
        this.rentalsSignal.update(rentals => [...rentals, newRental]);
      })
    );
  }

  cancel(rentalId: number): Observable<Rental> {
    return this.http.patch<Rental>(`${this.apiUrl}/${rentalId}/cancel`, {}).pipe(
      tap(updatedRental => {
        this.rentalsSignal.update(rentals =>
          rentals.map(r => r.id === rentalId ? updatedRental : r)
        );
      })
    );
  }

  refreshRentals(userId: number): void {
    this.getByUser(userId).subscribe();
  }
}
