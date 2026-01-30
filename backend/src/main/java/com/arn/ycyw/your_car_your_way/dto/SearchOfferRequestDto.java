package com.arn.ycyw.your_car_your_way.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchOfferRequestDto {
    
    @Schema(description = "Ville de départ", example = "Paris")
    @NotNull(message = "La ville de départ est obligatoire")
    private String departureCity;
    
    @Schema(description = "Ville de retour", example = "Lyon")
    @NotNull(message = "La ville de retour est obligatoire")
    private String returnCity;
    
    @Schema(description = "Date et heure de début de location")
    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;
    
    @Schema(description = "Date et heure de fin de location")
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;
    
    @Schema(description = "Catégorie du véhicule (code ACRISS)", example = "CCAR")
    private String catCar;
}
