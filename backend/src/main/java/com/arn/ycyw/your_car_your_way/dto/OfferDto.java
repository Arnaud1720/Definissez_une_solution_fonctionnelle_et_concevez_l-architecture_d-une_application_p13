package com.arn.ycyw.your_car_your_way.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferDto {
    
    @Schema(description = "ID de l'offre")
    private Integer id;
    
    @Schema(description = "Agence de départ")
    private AgencyDto departureAgency;
    
    @Schema(description = "Agence de retour")
    private AgencyDto returnAgency;
    
    @Schema(description = "Date et heure de début de location")
    private LocalDateTime startDate;
    
    @Schema(description = "Date et heure de fin de location")
    private LocalDateTime endDate;
    
    @Schema(description = "Catégorie du véhicule (code ACRISS)")
    private String catCar;
    
    @Schema(description = "Prix de la location en centimes")
    private Integer price;
}
