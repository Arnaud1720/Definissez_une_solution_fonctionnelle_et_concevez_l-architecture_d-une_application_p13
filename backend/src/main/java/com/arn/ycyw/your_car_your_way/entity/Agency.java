package com.arn.ycyw.your_car_your_way.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "agencys")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Agency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 100, nullable = false)
    private String city;

    @Column(length = 100, nullable = false)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @OneToMany(mappedBy = "departureAgency")
    private List<Rentals> departureRentals = new ArrayList<>();

    @OneToMany(mappedBy = "returnAgency")
    private List<Rentals> returnRentals = new ArrayList<>();
}
