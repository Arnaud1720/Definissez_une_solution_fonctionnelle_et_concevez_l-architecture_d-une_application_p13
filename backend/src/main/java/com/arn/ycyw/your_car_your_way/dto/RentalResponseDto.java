package com.arn.ycyw.your_car_your_way.dto;

import com.arn.ycyw.your_car_your_way.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les réservations, incluant les agences complètes
 * Utilisé pour l'affichage dans le frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponseDto {

    private Integer id;
    private String catCar;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer price;
    private Status status;

    private AgencyDto departureAgency;
    private AgencyDto returnAgency;

    private Integer refundPercentage;
}
