package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.dto.RentalResponseDto;
import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Status;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.exception.BusinessException;
import com.arn.ycyw.your_car_your_way.mapper.RentalsMapper;
import com.arn.ycyw.your_car_your_way.reposiory.AgencyRepository;
import com.arn.ycyw.your_car_your_way.reposiory.RentalRepository;
import com.arn.ycyw.your_car_your_way.reposiory.UserRepository;
import com.arn.ycyw.your_car_your_way.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RantalServiceImpl Tests")
class RantalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalsMapper rentalsMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RantalServiceImpl rantalService;

    private Users user;
    private Agency departureAgency;
    private Agency returnAgency;
    private Rentals rental;
    private RentalsDto rentalsDto;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setId(1);
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        departureAgency = new Agency();
        departureAgency.setId(1);
        departureAgency.setName("Paris CDG");
        departureAgency.setCity("Paris");
        departureAgency.setCountry("France");

        returnAgency = new Agency();
        returnAgency.setId(2);
        returnAgency.setName("Lyon Part-Dieu");
        returnAgency.setCity("Lyon");
        returnAgency.setCountry("France");

        rental = new Rentals();
        rental.setId(1);
        rental.setCatCar("B");
        rental.setStartDate(LocalDateTime.now().plusDays(10));
        rental.setEndDate(LocalDateTime.now().plusDays(15));
        rental.setPrice(15000);
        rental.setStatus(Status.BOOKED);
        rental.setUser(user);
        rental.setDepartureAgency(departureAgency);
        rental.setReturnAgency(returnAgency);

        rentalsDto = new RentalsDto();
        rentalsDto.setId(1);
        rentalsDto.setCatCar("B");
        rentalsDto.setPrice(15000);
        rentalsDto.setUserId(1);
        rentalsDto.setDepartureAgencyId(1);
        rentalsDto.setReturnAgencyId(2);
    }

    @Nested
    @DisplayName("Tests de creation de reservation")
    class SaveRentalTests {

        @ParameterizedTest(name = "Creation avec categorie {0} et prix {1}")
        @CsvSource({
            "A, 10000",
            "B, 15000",
            "C, 20000",
            "D, 25000",
            "E, 35000"
        })
        @DisplayName("Test parametrise: differentes categories de vehicules")
        void saveRental_WithDifferentCategories_ShouldSaveSuccessfully(String category, int price) {
            // Given
            rentalsDto.setCatCar(category);
            rentalsDto.setPrice(price);
            rental.setCatCar(category);
            rental.setPrice(price);

            // userId=1, departureAgencyId=1, returnAgencyId=2 from setUp
            lenient().when(userRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(user));
            lenient().when(agencyRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(departureAgency));
            lenient().when(agencyRepository.findById(Integer.valueOf(2))).thenReturn(Optional.of(returnAgency));
            when(rentalsMapper.toEntity(any(RentalsDto.class))).thenReturn(rental);
            when(rentalRepository.save(any(Rentals.class))).thenReturn(rental);
            when(rentalsMapper.toDto(any(Rentals.class))).thenReturn(rentalsDto);

            // When
            RentalsDto result = rantalService.saveRental(rentalsDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCatCar()).isEqualTo(category);
            assertThat(result.getPrice()).isEqualTo(price);

            ArgumentCaptor<Rentals> rentalCaptor = ArgumentCaptor.forClass(Rentals.class);
            verify(rentalRepository).save(rentalCaptor.capture());
            assertThat(rentalCaptor.getValue().getStatus()).isEqualTo(Status.BOOKED);
        }

        @Test
        @DisplayName("Creation avec utilisateur inexistant doit lancer BusinessException")
        void saveRental_WithNonExistingUser_ShouldThrowException() {
            // Given - userId is 1 from setUp
            lenient().when(userRepository.findById(Integer.valueOf(1))).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rantalService.saveRental(rentalsDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Utilisateur non");
        }

        @Test
        @DisplayName("Creation avec agence depart inexistante doit lancer BusinessException")
        void saveRental_WithNonExistingDepartureAgency_ShouldThrowException() {
            // Given - userId is 1, departureAgencyId is 1 from setUp
            lenient().when(userRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(user));
            lenient().when(agencyRepository.findById(Integer.valueOf(1))).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rantalService.saveRental(rentalsDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Agence de");
        }
    }

    @Nested
    @DisplayName("Tests d'annulation de reservation")
    class CancelRentalTests {

        @ParameterizedTest(name = "Annulation {0} jours avant le depart doit donner {1}% de remboursement")
        @CsvSource({
            "1, 25",
            "3, 25",
            "6, 25",
            "7, 100",
            "10, 100",
            "30, 100"
        })
        @DisplayName("Test parametrise: calcul du pourcentage de remboursement")
        void cancelRental_ShouldCalculateCorrectRefundPercentage(int daysBeforeStart, int expectedRefundPercentage) {
            // Given
            rental.setStartDate(LocalDateTime.now().plusDays(daysBeforeStart));
            Integer rentalId = 1;
            Integer currentUserId = 1;

            when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
            when(rentalRepository.save(any(Rentals.class))).thenReturn(rental);
            when(rentalsMapper.toDto(any(Rentals.class))).thenReturn(rentalsDto);

            // When
            rantalService.cancelRental(rentalId, currentUserId);

            // Then
            ArgumentCaptor<Rentals> rentalCaptor = ArgumentCaptor.forClass(Rentals.class);
            verify(rentalRepository).save(rentalCaptor.capture());

            Rentals savedRental = rentalCaptor.getValue();
            assertThat(savedRental.getStatus()).isEqualTo(Status.CANCELLED);
            assertThat(savedRental.getRefundPercentage()).isEqualTo(expectedRefundPercentage);
        }

        @Test
        @DisplayName("Annulation par un autre utilisateur doit lancer AccessDeniedException")
        void cancelRental_ByDifferentUser_ShouldThrowAccessDeniedException() {
            // Given
            Integer rentalId = 1;
            Integer differentUserId = 999;

            when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

            // When & Then
            assertThatThrownBy(() -> rantalService.cancelRental(rentalId, differentUserId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("propres");
        }

        @Test
        @DisplayName("Annulation d'une reservation inexistante doit lancer BusinessException")
        void cancelRental_WithNonExistingRental_ShouldThrowException() {
            // Given
            when(rentalRepository.findById(999)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> rantalService.cancelRental(999, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("introuvable");
        }
    }

    @Nested
    @DisplayName("Tests de recuperation des reservations par utilisateur")
    class FindByUserIdTests {

        @ParameterizedTest(name = "Utilisateur avec {0} reservations")
        @CsvSource({
            "1",
            "5",
            "10"
        })
        @DisplayName("Test parametrise: nombre de reservations par utilisateur")
        void findAllByUserId_ShouldReturnCorrectNumberOfRentals(int numberOfRentals) {
            // Given
            Integer userId = 1;
            List<Rentals> rentalsList = createRentalsList(numberOfRentals);

            when(rentalRepository.findAllByUser_Id(userId)).thenReturn(rentalsList);
            when(rentalsMapper.toDto(any(Rentals.class))).thenReturn(rentalsDto);

            // When
            List<RentalsDto> result = rantalService.findAllByUserId(userId);

            // Then
            assertThat(result).hasSize(numberOfRentals);
            verify(rentalRepository).findAllByUser_Id(userId);
        }

        @Test
        @DisplayName("Utilisateur sans reservations doit retourner liste vide")
        void findAllByUserId_WithNoRentals_ShouldReturnEmptyList() {
            // Given
            Integer userId = 1;

            when(rentalRepository.findAllByUser_Id(userId)).thenReturn(List.of());

            // When
            List<RentalsDto> result = rantalService.findAllByUserId(userId);

            // Then
            assertThat(result).isEmpty();
            verify(rentalRepository).findAllByUser_Id(userId);
        }

        @ParameterizedTest(name = "Filtrage par statut {0}")
        @EnumSource(Status.class)
        @DisplayName("Test parametrise: reservations avec differents statuts")
        void findAllByUserIdWithAgencies_ShouldReturnRentalsWithCorrectStatus(Status status) {
            // Given
            Integer userId = 1;
            rental.setStatus(status);
            List<Rentals> rentalsList = List.of(rental);

            when(rentalRepository.findAllByUser_Id(userId)).thenReturn(rentalsList);

            // When
            List<RentalResponseDto> result = rantalService.findAllByUserIdWithAgencies(userId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("Tests de mise a jour de reservation")
    class UpdateRentalTests {

        @ParameterizedTest(name = "Modification {0} heures avant le depart doit {1}")
        @MethodSource("com.arn.ycyw.your_car_your_way.services.impl.RantalServiceImplTest#provideModificationTimeTestCases")
        @DisplayName("Test parametrise: delai de modification")
        void updateRental_ShouldRespectModificationDeadline(int hoursBeforeStart, boolean shouldSucceed) {
            // Given
            rental.setStartDate(LocalDateTime.now().plusHours(hoursBeforeStart));
            rentalsDto.setStartDate(LocalDateTime.now().plusHours(hoursBeforeStart));
            Integer currentUserId = 1;

            // rentalsDto.getId() = 1 from setUp
            when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));

            if (shouldSucceed) {
                when(agencyRepository.findById(1)).thenReturn(Optional.of(departureAgency));
                when(agencyRepository.findById(2)).thenReturn(Optional.of(returnAgency));
                when(rentalRepository.save(any(Rentals.class))).thenReturn(rental);
                when(rentalsMapper.toDto(any(Rentals.class))).thenReturn(rentalsDto);

                // When
                RentalsDto result = rantalService.updateRental(rentalsDto, currentUserId);

                // Then
                assertThat(result).isNotNull();
                verify(rentalRepository).save(any(Rentals.class));
            } else {
                // When & Then
                assertThatThrownBy(() -> rantalService.updateRental(rentalsDto, currentUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("48h");
            }
        }

        @Test
        @DisplayName("Modification par un autre utilisateur doit lancer AccessDeniedException")
        void updateRental_ByDifferentUser_ShouldThrowAccessDeniedException() {
            // Given
            Integer differentUserId = 999;
            rental.setStartDate(LocalDateTime.now().plusDays(10)); // Assez loin pour permettre la modification

            // rentalsDto.getId() = 1 from setUp
            when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));

            // When & Then - l'exception est lancee avant la verification des agences
            assertThatThrownBy(() -> rantalService.updateRental(rentalsDto, differentUserId))
                .isInstanceOf(AccessDeniedException.class);
        }
    }

    // Methodes utilitaires pour les tests

    private List<Rentals> createRentalsList(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                Rentals r = new Rentals();
                r.setId(i + 1);
                r.setCatCar("B");
                r.setStatus(Status.BOOKED);
                r.setUser(user);
                r.setDepartureAgency(departureAgency);
                r.setReturnAgency(returnAgency);
                return r;
            })
            .toList();
    }

    // Methode source pour les tests parametres de delai de modification
    static Stream<Arguments> provideModificationTimeTestCases() {
        return Stream.of(
            Arguments.of(24, false),   // 24h avant = trop tard
            Arguments.of(47, false),   // 47h avant = trop tard
            Arguments.of(49, true),    // 49h avant = OK
            Arguments.of(72, true),    // 72h avant = OK
            Arguments.of(168, true)    // 1 semaine avant = OK
        );
    }
}
