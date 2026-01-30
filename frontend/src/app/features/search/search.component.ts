import { CommonModule } from "@angular/common";
import { Component, inject, OnInit, signal } from "@angular/core";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatNativeDateModule } from "@angular/material/core";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { Router } from "@angular/router";
import { VEHICLE_CATEGORIES } from "@core/models";
import { AgencyService, OfferService } from "@core/services";

@Component({
  selector: "app-search",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    MatNativeDateModule,
  ],
  templateUrl: "./search.component.html",
  styleUrl: "./search.component.scss",
})
export class SearchComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private agencyService = inject(AgencyService);
  private offerService = inject(OfferService);

  form: FormGroup = this.fb.group({
    departureCity: ["", Validators.required],
    returnCity: ["", Validators.required],
    startDate: [null, Validators.required],
    endDate: [null, Validators.required],
    catCar: [""],
  });

  categories = VEHICLE_CATEGORIES;
  cityOptions = this.agencyService.cityOptions;
  isLoading = signal(false);
  errorMessage = signal("");
  minDate = new Date();
  minEndDate = new Date();

  ngOnInit(): void {
    this.agencyService.getAll().subscribe({
      error: (err) => {
        console.error("Erreur lors du chargement des agences", err);
        this.errorMessage.set("Impossible de charger les villes disponibles");
      },
    });

    // Mise à jour de la date minimum de retour quand la date de départ change
    this.form.get("startDate")?.valueChanges.subscribe((date) => {
      if (date) {
        this.minEndDate = new Date(date);
      }
    });
  }

  selectCategory(code: string): void {
    const current = this.form.get("catCar")?.value;
    this.form.patchValue({ catCar: current === code ? "" : code });
  }

  onSearch(): void {
    if (this.form.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set("");

    // Convertir les dates en format ISO string pour l'API
    const formValue = {
      ...this.form.value,
      startDate: this.form.value.startDate?.toISOString(),
      endDate: this.form.value.endDate?.toISOString(),
    };

    this.offerService.search(formValue).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(["/search/results"]);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          err.error?.message || "Aucune offre trouvée pour ces critères"
        );
      },
    });
  }
}
