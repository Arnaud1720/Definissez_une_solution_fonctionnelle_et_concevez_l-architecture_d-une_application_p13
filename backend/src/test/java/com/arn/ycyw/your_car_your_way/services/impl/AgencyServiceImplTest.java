package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.dto.AgencyDto;
import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.mapper.AgencyMapper;
import com.arn.ycyw.your_car_your_way.reposiory.AgencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgencyServiceImpl Tests")
class AgencyServiceImplTest {

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private AgencyMapper agencyMapper;

    @InjectMocks
    private AgencyServiceImpl agencyService;

    private Agency agency;
    private AgencyDto agencyDto;

    @BeforeEach
    void setUp() {
        agency = new Agency();
        agency.setId(1);
        agency.setName("Paris CDG");
        agency.setAddress("1 Avenue de l'Europe");
        agency.setCity("Paris");
        agency.setCountry("France");
        agency.setPostalCode("95700");
        agency.setPhone("+33 1 23 45 67 89");
        agency.setEmail("paris-cdg@ycyw.com");

        agencyDto = new AgencyDto();
        agencyDto.setId(1);
        agencyDto.setName("Paris CDG");
        agencyDto.setAddress("1 Avenue de l'Europe");
        agencyDto.setCity("Paris");
        agencyDto.setCountry("France");
        agencyDto.setPostalCode("95700");
        agencyDto.setPhone("+33 1 23 45 67 89");
        agencyDto.setEmail("paris-cdg@ycyw.com");
    }

    @Nested
    @DisplayName("Tests de creation d'agence")
    class SaveAgencyTests {

        @ParameterizedTest(name = "Creation agence dans la ville {0}, pays {1}")
        @CsvSource({
            "Paris, France",
            "Lyon, France",
            "Madrid, Espagne",
            "Berlin, Allemagne",
            "Rome, Italie",
            "Londres, Royaume-Uni"
        })
        @DisplayName("Test parametrise: creation d'agences dans differentes villes")
        void save_WithDifferentCities_ShouldSaveSuccessfully(String city, String country) {
            // Given
            agencyDto.setCity(city);
            agencyDto.setCountry(country);
            agency.setCity(city);
            agency.setCountry(country);

            when(agencyMapper.toEntity(any(AgencyDto.class))).thenReturn(agency);
            when(agencyRepository.save(any(Agency.class))).thenReturn(agency);
            when(agencyMapper.toDto(any(Agency.class))).thenReturn(agencyDto);

            // When
            AgencyDto result = agencyService.save(agencyDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCity()).isEqualTo(city);
            assertThat(result.getCountry()).isEqualTo(country);
            verify(agencyRepository).save(any(Agency.class));
        }

        @ParameterizedTest(name = "Creation agence avec nom: {0}")
        @MethodSource("com.arn.ycyw.your_car_your_way.services.impl.AgencyServiceImplTest#provideAgencyNames")
        @DisplayName("Test parametrise: differents noms d'agences")
        void save_WithDifferentNames_ShouldSaveSuccessfully(String name, String expectedName) {
            // Given
            agencyDto.setName(name);
            agency.setName(name);

            when(agencyMapper.toEntity(any(AgencyDto.class))).thenReturn(agency);
            when(agencyRepository.save(any(Agency.class))).thenReturn(agency);
            when(agencyMapper.toDto(any(Agency.class))).thenReturn(agencyDto);

            // When
            AgencyDto result = agencyService.save(agencyDto);

            // Then
            assertThat(result.getName()).isEqualTo(expectedName);
            verify(agencyRepository).save(any(Agency.class));
        }

        @Test
        @DisplayName("Save doit mettre l'ID a null avant sauvegarde")
        void save_ShouldSetIdToNullBeforeSaving() {
            // Given
            agencyDto.setId(999); // ID existant

            when(agencyMapper.toEntity(any(AgencyDto.class))).thenReturn(agency);
            when(agencyRepository.save(any(Agency.class))).thenReturn(agency);
            when(agencyMapper.toDto(any(Agency.class))).thenReturn(agencyDto);

            // When
            agencyService.save(agencyDto);

            // Then
            assertThat(agencyDto.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("Tests de recherche d'agence par ID")
    class FindByIdTests {

        @ParameterizedTest(name = "Recherche agence avec ID {0}")
        @CsvSource({
            "1",
            "5",
            "10",
            "100",
            "999"
        })
        @DisplayName("Test parametrise: recherche par differents IDs existants")
        void findById_WithExistingId_ShouldReturnAgency(int id) {
            // Given
            agency.setId(id);
            agencyDto.setId(id);

            when(agencyRepository.findById(id)).thenReturn(Optional.of(agency));
            when(agencyMapper.toDto(agency)).thenReturn(agencyDto);

            // When
            AgencyDto result = agencyService.finById(id);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            verify(agencyRepository).findById(id);
        }

        @ParameterizedTest(name = "Recherche agence inexistante avec ID {0}")
        @CsvSource({
            "-1",
            "0",
            "99999"
        })
        @DisplayName("Test parametrise: recherche par IDs inexistants")
        void findById_WithNonExistingId_ShouldReturnNull(int id) {
            // Given
            when(agencyRepository.findById(id)).thenReturn(Optional.empty());
            when(agencyMapper.toDto(null)).thenReturn(null);

            // When
            AgencyDto result = agencyService.finById(id);

            // Then
            assertThat(result).isNull();
            verify(agencyRepository).findById(id);
        }
    }

    @Nested
    @DisplayName("Tests de recuperation de toutes les agences")
    class FindAllTests {

        @ParameterizedTest(name = "Recuperation de {0} agences")
        @CsvSource({
            "1",
            "5",
            "10",
            "50"
        })
        @DisplayName("Test parametrise: differents nombres d'agences")
        void findAll_ShouldReturnCorrectNumberOfAgencies(int numberOfAgencies) {
            // Given
            List<Agency> agencies = createAgenciesList(numberOfAgencies);
            when(agencyRepository.findAll()).thenReturn(agencies);
            when(agencyMapper.toDto(any(Agency.class))).thenReturn(agencyDto);

            // When
            List<AgencyDto> result = agencyService.findAll();

            // Then
            assertThat(result).hasSize(numberOfAgencies);
            verify(agencyRepository).findAll();
        }

        @Test
        @DisplayName("FindAll avec liste vide doit retourner liste vide")
        void findAll_WithEmptyList_ShouldReturnEmptyList() {
            // Given
            when(agencyRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<AgencyDto> result = agencyService.findAll();

            // Then
            assertThat(result).isEmpty();
        }

        @ParameterizedTest(name = "Filtrage par pays: {0}")
        @MethodSource("com.arn.ycyw.your_car_your_way.services.impl.AgencyServiceImplTest#provideCountriesWithAgencyCount")
        @DisplayName("Test parametrise: agences par pays")
        void findAll_ShouldReturnAgenciesFromDifferentCountries(String country, int expectedCount) {
            // Given
            List<Agency> agencies = createAgenciesForCountry(country, expectedCount);
            when(agencyRepository.findAll()).thenReturn(agencies);
            when(agencyMapper.toDto(any(Agency.class))).thenAnswer(invocation -> {
                Agency a = invocation.getArgument(0);
                AgencyDto dto = new AgencyDto();
                dto.setCountry(a.getCountry());
                return dto;
            });

            // When
            List<AgencyDto> result = agencyService.findAll();

            // Then
            assertThat(result).hasSize(expectedCount);
            assertThat(result).allMatch(dto -> dto.getCountry().equals(country));
        }
    }

    // Methodes utilitaires pour les tests

    private List<Agency> createAgenciesList(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                Agency a = new Agency();
                a.setId(i + 1);
                a.setName("Agency " + (i + 1));
                a.setCity("City " + (i + 1));
                a.setCountry("France");
                return a;
            })
            .toList();
    }

    private List<Agency> createAgenciesForCountry(String country, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                Agency a = new Agency();
                a.setId(i + 1);
                a.setName("Agency " + country + " " + (i + 1));
                a.setCity("City " + (i + 1));
                a.setCountry(country);
                return a;
            })
            .toList();
    }

    // Methodes sources pour les tests parametres

    static Stream<Arguments> provideAgencyNames() {
        return Stream.of(
            Arguments.of("Paris CDG", "Paris CDG"),
            Arguments.of("Lyon Part-Dieu", "Lyon Part-Dieu"),
            Arguments.of("Marseille Saint-Charles", "Marseille Saint-Charles"),
            Arguments.of("Nice Cote d'Azur", "Nice Cote d'Azur"),
            Arguments.of("Bordeaux Merignac", "Bordeaux Merignac")
        );
    }

    static Stream<Arguments> provideCountriesWithAgencyCount() {
        return Stream.of(
            Arguments.of("France", 5),
            Arguments.of("Espagne", 3),
            Arguments.of("Allemagne", 4),
            Arguments.of("Italie", 2),
            Arguments.of("Belgique", 1)
        );
    }
}
