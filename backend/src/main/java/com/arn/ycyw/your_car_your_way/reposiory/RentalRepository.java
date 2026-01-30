package com.arn.ycyw.your_car_your_way.reposiory;


import com.arn.ycyw.your_car_your_way.entity.Rentals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rentals,Integer> {

    List<Rentals> findAllByUser_Id(Integer userId);
}
