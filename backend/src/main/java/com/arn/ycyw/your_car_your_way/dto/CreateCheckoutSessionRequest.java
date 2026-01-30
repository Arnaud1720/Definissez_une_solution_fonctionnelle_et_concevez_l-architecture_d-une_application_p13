package com.arn.ycyw.your_car_your_way.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutSessionRequest {
    private String catCar;
    private String startDate;
    private String endDate;
    private Integer price; // Prix HT en centimes
    private Integer departureAgencyId;
    private Integer returnAgencyId;
    private String departureAgencyName;
    private String returnAgencyName;
    private String departureCity;
    private String returnCity;
}
