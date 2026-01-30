package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.dto.AgencyDto;
import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.mapper.AgencyMapper;
import com.arn.ycyw.your_car_your_way.reposiory.AgencyRepository;
import com.arn.ycyw.your_car_your_way.services.AgencyService;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AgencyServiceImpl implements AgencyService {
    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;
    public AgencyServiceImpl(AgencyRepository agencyRepository, AgencyMapper agencyMapper) {
        this.agencyRepository = agencyRepository;
        this.agencyMapper = agencyMapper;
    }

    @Override
    public AgencyDto save(AgencyDto agencyDto) {
        agencyDto.setId(null);
        Agency agency =  agencyMapper.toEntity(agencyDto);
        Agency agencySaved = agencyRepository.save(agency);
        return agencyMapper.toDto(agencySaved);
    }

    @Override
    public AgencyDto finById(int id) {
        Agency agency = agencyRepository.findById(id).orElse(null);
        return agencyMapper.toDto(agency);
    }

    @Override
    public List<AgencyDto> findAll() {
        List<Agency> agencies = agencyRepository.findAll();
        return agencies.stream().map(agencyMapper::toDto).collect(toList());
    }
}
