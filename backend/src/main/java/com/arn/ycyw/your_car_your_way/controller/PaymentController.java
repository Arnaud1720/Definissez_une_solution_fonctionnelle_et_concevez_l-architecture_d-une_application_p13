package com.arn.ycyw.your_car_your_way.controller;

import com.arn.ycyw.your_car_your_way.dto.CreateCheckoutSessionRequest;
import com.stripe.exception.StripeException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    @Value("${stripe.frontend-url}")
    private String frontendUrl;

    // Taux de TVA (20% en France)
    private static final double TVA_RATE = 0.20;

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody CreateCheckoutSessionRequest request) throws StripeException {

        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Calcul des montants
        long priceHT = request.getPrice(); // Prix HT en centimes
        long tvaAmount = Math.round(priceHT * TVA_RATE); // TVA en centimes
        long priceTTC = priceHT + tvaAmount; // Prix TTC en centimes

        // Description de la location
        String description = String.format("Location véhicule %s - Du %s au %s - %s → %s",
                request.getCatCar(),
                formatDate(request.getStartDate()),
                formatDate(request.getEndDate()),
                request.getDepartureCity(),
                request.getReturnCity()
        );

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setCustomerEmail(userEmail)
                // Ligne principale : Location HT
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(priceHT)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Location de véhicule - Catégorie " + request.getCatCar())
                                                                .setDescription(description)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                // Ligne TVA
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(tvaAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("TVA (20%)")
                                                                .setDescription("Taxe sur la Valeur Ajoutée")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                // Métadonnées pour le webhook
                .putMetadata("catCar", request.getCatCar())
                .putMetadata("startDate", request.getStartDate())
                .putMetadata("endDate", request.getEndDate())
                .putMetadata("priceHT", String.valueOf(priceHT))
                .putMetadata("tvaAmount", String.valueOf(tvaAmount))
                .putMetadata("priceTTC", String.valueOf(priceTTC))
                .putMetadata("departureAgencyId", request.getDepartureAgencyId().toString())
                .putMetadata("returnAgencyId", request.getReturnAgencyId().toString())
                .putMetadata("departureAgencyName", request.getDepartureAgencyName())
                .putMetadata("returnAgencyName", request.getReturnAgencyName())
                .putMetadata("departureCity", request.getDepartureCity())
                .putMetadata("returnCity", request.getReturnCity())
                .putMetadata("userEmail", userEmail)
                .setSuccessUrl(frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/payment/cancel")
                .build();

        Session session = Session.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("url", session.getUrl());
        response.put("sessionId", session.getId());
        return response;
    }

    /**
     * Formate une date ISO en format lisible
     */
    private String formatDate(String isoDate) {
        try {
            // Format: 2025-12-27T15:09 -> 27/12/2025 15:09
            String[] parts = isoDate.split("T");
            String[] dateParts = parts[0].split("-");
            String time = parts.length > 1 ? parts[1] : "00:00";
            return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0] + " " + time;
        } catch (Exception e) {
            return isoDate;
        }
    }

}
