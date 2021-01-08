package com.udacity.boogle.maps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Get a list of all vehicle ids
     * @return
     */
    @Query("SELECT v.id FROM #{#entityName} v")
    List<Long> findAllId();
}
