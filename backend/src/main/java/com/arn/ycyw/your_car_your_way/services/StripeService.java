package com.arn.ycyw.your_car_your_way.services;

public interface StripeService {

    /**
     * Traite un webhook Stripe
     * @param payload Le payload JSON brut
     * @param sigHeader La signature Stripe
     * @return true si traité avec succès, false sinon
     */
    boolean handleWebhook(String payload, String sigHeader);
}
