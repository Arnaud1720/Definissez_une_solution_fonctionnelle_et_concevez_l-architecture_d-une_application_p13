package com.arn.ycyw.your_car_your_way.controller;

import com.arn.ycyw.your_car_your_way.dto.OfferDto;
import com.arn.ycyw.your_car_your_way.dto.SearchOfferRequestDto;
import com.arn.ycyw.your_car_your_way.services.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@Tag(name = "Offers", description = "API de recherche d'offres de location")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping("/search")
    @Operation(summary = "Rechercher des offres de location",
            description = "Recherche des offres disponibles selon les critères : ville de départ, ville de retour, dates et catégorie de véhicule")
    public ResponseEntity<List<OfferDto>> searchOffers(@Valid @RequestBody SearchOfferRequestDto searchRequest) {
        List<OfferDto> offers = offerService.searchOffers(searchRequest);
        return ResponseEntity.ok(offers);
    }

//    @GetMapping("/{id}")
//    @Operation(summary = "Consulter le détail d'une offre")
//    public ResponseEntity<OfferDto> getOfferById(@PathVariable Integer id) {
//        OfferDto offer = offerService.getOfferById(id);
//        return ResponseEntity.ok(offer);
//    }
}
