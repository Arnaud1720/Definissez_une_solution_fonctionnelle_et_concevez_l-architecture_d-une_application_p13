package com.arn.ycyw.your_car_your_way.reposiory;

import com.arn.ycyw.your_car_your_way.entity.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Integer> {
    List<Agency> findByCityIgnoreCase(String city);

}
