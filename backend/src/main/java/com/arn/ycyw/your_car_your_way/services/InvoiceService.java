package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Users;

public interface InvoiceService {

    /**
     * Génère une facture PDF pour une réservation
     *
     * @param user L'utilisateur
     * @param rental La réservation
     * @param departureAgency L'agence de départ
     * @param returnAgency L'agence de retour
     * @param priceHT Prix HT en centimes
     * @param tvaAmount Montant TVA en centimes
     * @param priceTTC Prix TTC en centimes
     * @return byte[] contenant le PDF
     */
    byte[] generateInvoicePdf(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            long priceHT,
            long tvaAmount,
            long priceTTC
    );
}
