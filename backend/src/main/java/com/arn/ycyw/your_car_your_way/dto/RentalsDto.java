package com.arn.ycyw.your_car_your_way.dto;

import com.arn.ycyw.your_car_your_way.entity.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalsDto {
    @Schema(description = "ID généré par le serveur", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @Schema(description = "Catégorie du véhicule (code ACRISS)", example = "CCAR")
    @NotNull(message = "La catégorie du véhicule est obligatoire")
    private String catCar;

    @Schema(description = "Date et heure de début de location")
    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    @Schema(description = "Date et heure de fin de location")
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    @Schema(description = "Prix de la location en centimes")
    private Integer price;

    @Schema(description = "Statut de la réservation")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Status status;

    @Schema(description = "ID de l'agence de départ")
    @NotNull(message = "L'agence de départ est obligatoire")
    private Integer departureAgencyId;

    @Schema(description = "ID de l'agence de retour")
    @NotNull(message = "L'agence de retour est obligatoire")
    private Integer returnAgencyId;

    @Schema(description = "ID de l'utilisateur")
    private Integer userId;

    @Schema(description = "Pourcentage de remboursement", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer refundPercentage;

}
