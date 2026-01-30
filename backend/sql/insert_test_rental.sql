-- Script pour insérer une réservation de test
-- Remplacez les IDs par ceux qui existent dans votre base

-- D'abord, vérifiez vos données existantes :
-- SELECT id, email FROM users;
-- SELECT id, name, city FROM agency;

-- Insérer une réservation de test (ajustez les IDs selon votre base)
INSERT INTO rentals (cat_car, date_de_debut, end_date, price, status, user_id, departure_agency_id, return_agency_id, refund_percentage)
VALUES (
    'B',                                    -- Catégorie du véhicule
    '2025-02-15 10:00:00',                 -- Date de début
    '2025-02-20 10:00:00',                 -- Date de fin
    15000,                                  -- Prix en centimes (150€)
    'BOOKED',                              -- Statut: BOOKED, COMPLETED, CANCELLED
    1,                                      -- user_id (remplacez par l'ID de votre utilisateur)
    1,                                      -- departure_agency_id
    1,                                      -- return_agency_id
    NULL                                    -- refund_percentage
);

-- Vérifier l'insertion
SELECT r.*, u.email, da.name as departure_agency, ra.name as return_agency
FROM rentals r
JOIN users u ON r.user_id = u.id
JOIN agency da ON r.departure_agency_id = da.id
JOIN agency ra ON r.return_agency_id = ra.id;
