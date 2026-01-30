package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.dto.AgencyDto;
import com.arn.ycyw.your_car_your_way.dto.OfferDto;
import com.arn.ycyw.your_car_your_way.dto.SearchOfferRequestDto;
import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.exception.BusinessException;
import com.arn.ycyw.your_car_your_way.mapper.AgencyMapper;
import com.arn.ycyw.your_car_your_way.reposiory.AgencyRepository;
import com.arn.ycyw.your_car_your_way.services.OfferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional(readOnly = true)
public class OfferServiceImpl implements OfferService {
    
    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;
    
    // Catégories de véhicules disponibles (codes ACRISS simplifiés)
    private static final List<String> VEHICLE_CATEGORIES = Arrays.asList(
            "ECAR", // Economy Car
            "CCAR", // Compact Car
            "ICAR", // Intermediate Car
            "SCAR", // Standard Car
            "FCAR", // Full-size Car
            "PCAR", // Premium Car
            "LCAR", // Luxury Car
            "MVAR", // Mini Van
            "FVAR", // Full-size Van
            "SUVR"  // SUV
    );
    
    private static final int[] BASE_PRICES = {
            2500,  // ECAR
            3500,  // CCAR
            4500,  // ICAR
            5500,  // SCAR
            6500,  // FCAR
            8500,  // PCAR
            12000, // LCAR
            7500,  // MVAR
            9500,  // FVAR
            8000   // SUVR
    };
    
    public OfferServiceImpl(AgencyRepository agencyRepository, AgencyMapper agencyMapper) {
        this.agencyRepository = agencyRepository;
        this.agencyMapper = agencyMapper;
    }
    
    @Override
    public List<OfferDto> searchOffers(SearchOfferRequestDto searchRequest) {
        // 1. Trouver les agences dans la ville de départ
        List<Agency> departureAgencies = agencyRepository.findByCityIgnoreCase(searchRequest.getDepartureCity());
        if (departureAgencies.isEmpty()) {
            throw new BusinessException("Aucune agence trouvée dans la ville de départ : " + searchRequest.getDepartureCity());
        }
        
        // 2. Trouver les agences dans la ville de retour
        List<Agency> returnAgencies = agencyRepository.findByCityIgnoreCase(searchRequest.getReturnCity());
        if (returnAgencies.isEmpty()) {
            throw new BusinessException("Aucune agence trouvée dans la ville de retour : " + searchRequest.getReturnCity());
        }
        
        // 3. Calculer le nombre de jours de location
        long days = ChronoUnit.DAYS.between(
                searchRequest.getStartDate().toLocalDate(), 
                searchRequest.getEndDate().toLocalDate()
        );
        if (days <= 0) {
            days = 1; // Minimum 1 jour
        }
        
        // 4. Générer les offres
        List<OfferDto> offers = new ArrayList<>();
        AtomicInteger offerId = new AtomicInteger(1);
        
        // Filtrer les catégories si une catégorie spécifique est demandée
        List<String> categoriesToSearch = searchRequest.getCatCar() != null && !searchRequest.getCatCar().isBlank()
                ? List.of(searchRequest.getCatCar().toUpperCase())
                : VEHICLE_CATEGORIES;
        
        for (Agency departureAgency : departureAgencies) {
            for (Agency returnAgency : returnAgencies) {
                for (int i = 0; i < categoriesToSearch.size(); i++) {
                    String category = categoriesToSearch.get(i);
                    int categoryIndex = VEHICLE_CATEGORIES.indexOf(category);
                    if (categoryIndex == -1) {
                        categoryIndex = 0; // Default to economy if category not found
                    }
                    
                    // Calculer le prix (prix de base * nombre de jours + supplément si aller-retour différent)
                    int basePrice = BASE_PRICES[categoryIndex];
                    int totalPrice = (int) (basePrice * days);
                    
                    // Ajouter un supplément de 15% si les villes de départ et retour sont différentes
                    if (!departureAgency.getCity().equalsIgnoreCase(returnAgency.getCity())) {
                        totalPrice = (int) (totalPrice * 1.15);
                    }
                    
                    OfferDto offer = OfferDto.builder()
                            .id(offerId.getAndIncrement())
                            .departureAgency(agencyMapper.toDto(departureAgency))
                            .returnAgency(agencyMapper.toDto(returnAgency))
                            .startDate(searchRequest.getStartDate())
                            .endDate(searchRequest.getEndDate())
                            .catCar(category)
                            .price(totalPrice)
                            .build();
                    
                    offers.add(offer);
                }
            }
        }
        
        return offers;
    }
    
//    @Override
//    public OfferDto getOfferById(Integer id) {
//        // Dans une vraie application, on aurait une entité Offer en base de données
//        // Pour le MVP, on retourne une erreur car les offres sont générées dynamiquement
//        throw new BusinessException("Pour consulter une offre, veuillez effectuer une recherche avec les critères souhaités.");
//    }
}
