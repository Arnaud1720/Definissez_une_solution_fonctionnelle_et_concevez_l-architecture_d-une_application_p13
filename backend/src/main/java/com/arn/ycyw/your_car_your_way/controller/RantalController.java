package com.arn.ycyw.your_car_your_way.controller;

import com.arn.ycyw.your_car_your_way.dto.RentalResponseDto;
import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.security.UsersDetailsAdapter;
import com.arn.ycyw.your_car_your_way.services.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/rantals")
public class RantalController {
    private final RentalService rentalService;

    public RantalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    /**
     * Créer une nouvelle réservation
     * POST /api/rantals/save
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveRantal(@Valid @RequestBody RentalsDto rentalsDto) {
        RentalsDto savedRentalsDto = rentalService.saveRental(rentalsDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedRentalsDto.getId())
                .toUri();

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Rental created!");
        body.put("rental", savedRentalsDto);

        return ResponseEntity.created(location).body(body);
    }

    /**
     * Récupérer une réservation par ID
     * GET /api/rantals/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RentalsDto> getRantalById(@PathVariable("id") int id) {
        RentalsDto rentalsDto = rentalService.getRentalById(id);
        return ResponseEntity.ok(rentalsDto);
    }

    /**
     * Récupérer les réservations de l'utilisateur connecté
     * GET /api/rantals/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<RentalResponseDto>> getMyRentals(
            @AuthenticationPrincipal UsersDetailsAdapter principal) {
        Integer currentUserId = principal.getUser().getId();
        List<RentalResponseDto> rentals = rentalService.findAllByUserIdWithAgencies(currentUserId);
        return ResponseEntity.ok(rentals);
    }

    /**
     * Récupérer les réservations par userId
     * GET /api/rantals/user/{userId}
     * ⚠️ Endpoint utilisé par le frontend
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RentalResponseDto>> getRentalsByUserId(
            @PathVariable Integer userId,
            @AuthenticationPrincipal UsersDetailsAdapter principal) {

        // Vérifier que l'utilisateur demande ses propres réservations
        Integer currentUserId = principal.getUser().getId();
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        List<RentalResponseDto> rentals = rentalService.findAllByUserIdWithAgencies(userId);
        return ResponseEntity.ok(rentals);
    }

    /**
     * Mettre à jour une réservation
     * PUT /api/rantals/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RentalsDto> updateRental(
            @PathVariable Integer id,
            @RequestBody RentalsDto dto,
            @AuthenticationPrincipal UsersDetailsAdapter principal) {
        dto.setId(id);
        Integer currentUserId = principal.getUser().getId();
        RentalsDto updated = rentalService.updateRental(dto, currentUserId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Annuler une réservation
     * PATCH /api/rantals/{id}/cancel
     * Envoie automatiquement un email de confirmation d'annulation
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RentalResponseDto> cancelRental(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsersDetailsAdapter principal) {
        Integer currentUserId = principal.getUser().getId();
        RentalResponseDto cancelled = rentalService.cancelRentalWithAgencies(id, currentUserId);
        return ResponseEntity.ok(cancelled);
    }
}
