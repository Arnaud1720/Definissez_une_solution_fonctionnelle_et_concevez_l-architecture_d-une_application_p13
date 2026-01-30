package com.arn.ycyw.your_car_your_way.mapper;

import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalsMapper {

    @Mapping(source = "departureAgency.id", target = "departureAgencyId")
    @Mapping(source = "returnAgency.id", target = "returnAgencyId")
    @Mapping(source = "user.id", target = "userId")
    RentalsDto toDto(Rentals rentals);

    @Mapping(target = "departureAgency", ignore = true)
    @Mapping(target = "returnAgency", ignore = true)
    @Mapping(target = "user", ignore = true)
    Rentals toEntity(RentalsDto dto);
}
