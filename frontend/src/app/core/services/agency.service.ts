import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, shareReplay } from 'rxjs';
import { environment } from '@env/environment';
import { Agency, CityOption } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AgencyService {
  private readonly apiUrl = `${environment.apiUrl}/agency`;

  private agenciesSignal = signal<Agency[]>([]);
  private agenciesCache$: Observable<Agency[]> | null = null;

  readonly agencies = this.agenciesSignal.asReadonly();

  readonly cityOptions = computed<CityOption[]>(() => {
    const agencies = this.agenciesSignal();
    const cityMap = new Map<string, CityOption>();

    agencies.forEach(agency => {
      const key = `${agency.city}-${agency.country}`;
      if (!cityMap.has(key)) {
        cityMap.set(key, {
          city: agency.city,
          country: agency.country,
          label: `${agency.city}, ${agency.country}`
        });
      }
    });

    return Array.from(cityMap.values()).sort((a, b) =>
      a.label.localeCompare(b.label)
    );
  });

  readonly countries = computed<string[]>(() => {
    const agencies = this.agenciesSignal();
    const countries = new Set(agencies.map(a => a.country));
    return Array.from(countries).sort();
  });

  constructor(private http: HttpClient) {}

  getAll(): Observable<Agency[]> {
    if (!this.agenciesCache$) {
      this.agenciesCache$ = this.http.get<Agency[]>(`${this.apiUrl}/all`).pipe(
        tap(agencies => this.agenciesSignal.set(agencies)),
        shareReplay(1)
      );
    }
    return this.agenciesCache$;
  }

  getById(id: number): Observable<Agency> {
    return this.http.get<Agency>(`${this.apiUrl}/${id}`);
  }

  getCities(): string[] {
    const cities = new Set(this.agenciesSignal().map(a => a.city));
    return Array.from(cities).sort();
  }

  getCitiesByCountry(country: string): CityOption[] {
    return this.cityOptions().filter(c =>
      c.country.toLowerCase() === country.toLowerCase()
    );
  }

  getAgenciesByCity(city: string): Agency[] {
    return this.agenciesSignal().filter(a =>
      a.city.toLowerCase() === city.toLowerCase()
    );
  }

  getAgenciesByCityAndCountry(city: string, country: string): Agency[] {
    return this.agenciesSignal().filter(a =>
      a.city.toLowerCase() === city.toLowerCase() &&
      a.country.toLowerCase() === country.toLowerCase()
    );
  }
}
