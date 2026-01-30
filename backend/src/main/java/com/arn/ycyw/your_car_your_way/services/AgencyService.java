package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.dto.AgencyDto;

import java.util.List;

public interface AgencyService {
    AgencyDto save(AgencyDto agencyDto);
    AgencyDto finById(int id);
    List<AgencyDto> findAll();
}
