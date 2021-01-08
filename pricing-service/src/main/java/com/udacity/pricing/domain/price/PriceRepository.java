package com.udacity.pricing.domain.price;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends CrudRepository<Price, Long> {

    @Query("SELECT p FROM #{#entityName} p WHERE p.vehicleId=:vehicleId")
    Price findPriceByVehicleId(Long vehicleId);

    long deleteByVehicleId(Long vehicleId);

}
