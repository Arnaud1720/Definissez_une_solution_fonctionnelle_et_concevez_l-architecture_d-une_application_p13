package com.arn.ycyw.your_car_your_way.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rentals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cat_car")
    private String catCar;

    @Column(name = "date_de_debut", nullable = false)  // ← Garder l'ancien nom
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column
    private Integer price;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "departure_agency_id", nullable = false)
    private Agency departureAgency;

    @ManyToOne
    @JoinColumn(name = "return_agency_id", nullable = false)
    private Agency returnAgency;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "refund_percentage")
    private Integer refundPercentage;
}
