package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.dto.RentalResponseDto;
import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.entity.Rentals;

import java.util.List;
/**
 * Service pour la gestion des réservations (Rentals)
 */
public interface RentalService {

    List<RentalsDto> findall();

    RentalsDto saveRental(RentalsDto rentalsDto);

    void delete(RentalsDto rentalsDto);

    RentalsDto updateRental(RentalsDto rentalsDto, Integer currentUserId);

    RentalsDto getRentalById(int id);

    List<RentalsDto> findAllByUserId(Integer userId);

    RentalsDto cancelRental(Integer id, Integer currentUserId);

    /**
     * Récupère toutes les réservations d'un utilisateur avec les agences complètes
     * C'est cette méthode que le frontend utilise pour afficher les réservations
     */
    List<RentalResponseDto> findAllByUserIdWithAgencies(Integer userId);

    /**
     * Annule une réservation et retourne la réponse avec les agences complètes
     * Envoie également un email de confirmation d'annulation
     */
    RentalResponseDto cancelRentalWithAgencies(Integer id, Integer currentUserId);
}
