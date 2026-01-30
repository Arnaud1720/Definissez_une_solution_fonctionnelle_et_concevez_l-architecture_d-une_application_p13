package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.services.StripeService;
import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Status;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.reposiory.AgencyRepository;
import com.arn.ycyw.your_car_your_way.reposiory.RentalRepository;
import com.arn.ycyw.your_car_your_way.reposiory.UserRepository;
import com.arn.ycyw.your_car_your_way.services.EmailService;
import com.arn.ycyw.your_car_your_way.services.InvoiceService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionRetrieveParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Transactional
public class StripeServiceImpl implements StripeService {


    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final RentalRepository rentalRepository;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final InvoiceService invoiceService;

    // Cache pour éviter les doublons
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();
    private final Set<String> processedSessionIds = ConcurrentHashMap.newKeySet();

    //  Lock pour synchroniser l'accès et éviter les doublons concurrents
    private final ReentrantLock webhookLock = new ReentrantLock();

    public StripeServiceImpl(RentalRepository rentalRepository,
                             AgencyRepository agencyRepository,
                             UserRepository userRepository,
                             EmailService emailService,
                             InvoiceService invoiceService) {
        this.rentalRepository = rentalRepository;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.invoiceService = invoiceService;
    }

    @Override
    @Transactional
    public boolean handleWebhook(String payload, String sigHeader) {
        System.out.println("=== WEBHOOK REÇU ===");
        System.out.println("Payload length = " + payload.length());

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            System.out.println("✅ Signature validée ! Event type = " + event.getType());
        } catch (Exception e) {
            System.out.println("❌ Erreur signature : " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // Ignorer les événements non pertinents
        if (!"checkout.session.completed".equals(event.getType())) {
            System.out.println("⏭️ Event ignoré : " + event.getType());
            return true;
        }

        System.out.println("🎯 C'est un checkout.session.completed !");

        String eventId = event.getId();
        String sessionId = extractSessionIdFromPayload(payload);
        System.out.println("📋 Event ID : " + eventId);
        System.out.println("📋 Session ID extrait : " + sessionId);

        // 🔒 SECTION CRITIQUE : Utiliser un lock pour éviter les doublons concurrents
        webhookLock.lock();
        try {
            // Vérification anti-doublon APRÈS avoir acquis le lock
            if (processedEventIds.contains(eventId)) {
                System.out.println("⏭️ Événement déjà traité (eventId), ignoré : " + eventId);
                return true;
            }

            if (sessionId != null && processedSessionIds.contains(sessionId)) {
                System.out.println("⏭️ Session déjà traitée (sessionId), ignorée : " + sessionId);
                return true;
            }

            // Marquer IMMÉDIATEMENT comme en cours de traitement (avant le traitement réel)
            // Cela empêche un autre thread de passer
            processedEventIds.add(eventId);
            if (sessionId != null) {
                processedSessionIds.add(sessionId);
            }
            System.out.println("🔒 Événement verrouillé pour traitement : " + eventId);

        } finally {
            webhookLock.unlock();
        }

        // Maintenant on peut traiter sans risque de doublon
        try {
            if (sessionId != null) {
                SessionRetrieveParams params = SessionRetrieveParams.builder()
                        .addExpand("line_items")
                        .build();
                Session session = Session.retrieve(sessionId, params, null);
                System.out.println("✅ Session récupérée via API !");
                System.out.println("📧 Customer email : " + session.getCustomerEmail());

                boolean success = processSuccessfulPayment(session);

                if (success) {
                    System.out.println("✅ Traitement terminé avec succès");
                    cleanupCaches();
                } else {
                    // En cas d'échec, retirer du cache pour permettre un retry
                    processedEventIds.remove(eventId);
                    if (sessionId != null) {
                        processedSessionIds.remove(sessionId);
                    }
                }

                return success;
            } else {
                System.out.println("❌ Impossible d'extraire l'ID de session");
                processedEventIds.remove(eventId);
                return false;
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la récupération de la session : " + e.getMessage());
            e.printStackTrace();
            // En cas d'exception, retirer du cache
            processedEventIds.remove(eventId);
            if (sessionId != null) {
                processedSessionIds.remove(sessionId);
            }
            return false;
        }
    }

    /**
     * Traite un paiement réussi : crée le rental, génère la facture, envoie l'email
     */
    private boolean processSuccessfulPayment(Session session) {
        try {
            Map<String, String> metadata = session.getMetadata();
            System.out.println("📦 Métadonnées reçues : " + metadata);

            if (metadata == null || metadata.isEmpty()) {
                System.out.println("❌ Pas de métadonnées dans la session !");
                return false;
            }

            // Récupérer les données des métadonnées
            String catCar = metadata.get("catCar");
            String startDateStr = metadata.get("startDate");
            String endDateStr = metadata.get("endDate");
            String priceHTStr = metadata.get("priceHT");
            String tvaAmountStr = metadata.get("tvaAmount");
            String priceTTCStr = metadata.get("priceTTC");
            String departureAgencyIdStr = metadata.get("departureAgencyId");
            String returnAgencyIdStr = metadata.get("returnAgencyId");
            String userEmail = metadata.get("userEmail");

            System.out.println("📋 Données extraites :");
            System.out.println("   - catCar: " + catCar);
            System.out.println("   - userEmail: " + userEmail);

            // Vérifier que toutes les données sont présentes
            if (catCar == null || startDateStr == null || endDateStr == null ||
                    priceHTStr == null || departureAgencyIdStr == null ||
                    returnAgencyIdStr == null || userEmail == null) {
                System.out.println("❌ Données manquantes dans les métadonnées !");
                return false;
            }

            long priceHT = Long.parseLong(priceHTStr);
            long tvaAmount = Long.parseLong(tvaAmountStr);
            long priceTTC = Long.parseLong(priceTTCStr);
            Integer departureAgencyId = Integer.parseInt(departureAgencyIdStr);
            Integer returnAgencyId = Integer.parseInt(returnAgencyIdStr);

            // Parser les dates
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

            // Récupérer les entités
            Optional<Users> userOpt = userRepository.findByEmail(userEmail);
            Optional<Agency> departureAgencyOpt = agencyRepository.findById(departureAgencyId);
            Optional<Agency> returnAgencyOpt = agencyRepository.findById(returnAgencyId);

            if (userOpt.isEmpty() || departureAgencyOpt.isEmpty() || returnAgencyOpt.isEmpty()) {
                System.out.println("❌ Données manquantes pour créer le rental");
                return false;
            }

            Users user = userOpt.get();
            Agency departureAgency = departureAgencyOpt.get();
            Agency returnAgency = returnAgencyOpt.get();

            // Créer le Rental
            Rentals rental = createRental(catCar, startDate, endDate, priceTTC,
                    departureAgency, returnAgency, user);

            System.out.println("✅ Rental créé avec succès : ID = " + rental.getId());

            // Générer la facture PDF
            byte[] invoicePdf = invoiceService.generateInvoicePdf(
                    user, rental, departureAgency, returnAgency,
                    priceHT, tvaAmount, priceTTC
            );
            System.out.println("📄 Facture PDF générée : " + invoicePdf.length + " bytes");

            // Envoyer l'email de confirmation
            emailService.sendBookingConfirmationWithInvoice(
                    user, rental, departureAgency, returnAgency,
                    priceHT, tvaAmount, priceTTC, invoicePdf
            );
            System.out.println("📧 Email de confirmation envoyé à " + user.getEmail());

            return true;

        } catch (Exception e) {
            System.out.println("❌ Erreur lors du traitement du paiement : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crée et sauvegarde un nouveau Rental
     */
    private Rentals createRental(String catCar, LocalDateTime startDate, LocalDateTime endDate,
                                 long priceTTC, Agency departureAgency, Agency returnAgency, Users user) {
        Rentals rental = new Rentals();
        rental.setCatCar(catCar);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setPrice((int) priceTTC);
        rental.setStatus(Status.BOOKED);
        rental.setDepartureAgency(departureAgency);
        rental.setReturnAgency(returnAgency);
        rental.setUser(user);

        return rentalRepository.save(rental);
    }

    /**
     * Extrait l'ID de la session depuis le payload JSON brut
     */
    private String extractSessionIdFromPayload(String payload) {
        try {
            int csIndex = payload.indexOf("\"cs_");
            if (csIndex != -1) {
                int start = csIndex + 1;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Erreur extraction session ID : " + e.getMessage());
            return null;
        }
    }

    /**
     * Nettoie les caches si trop grands (évite fuite mémoire)
     */
    private void cleanupCaches() {
        if (processedEventIds.size() > 1000) {
            System.out.println("🧹 Nettoyage du cache des événements");
            processedEventIds.clear();
        }
        if (processedSessionIds.size() > 1000) {
            System.out.println("🧹 Nettoyage du cache des sessions");
            processedSessionIds.clear();
        }
    }
}
