package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.dto.OfferDto;
import com.arn.ycyw.your_car_your_way.dto.SearchOfferRequestDto;

import java.util.List;

public interface OfferService {
    List<OfferDto> searchOffers(SearchOfferRequestDto searchRequest);
//    OfferDto getOfferById(Integer id);
}
