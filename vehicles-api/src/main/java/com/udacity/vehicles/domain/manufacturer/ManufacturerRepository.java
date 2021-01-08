package com.udacity.vehicles.domain.manufacturer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Integer> {

    @Query("SELECT m FROM #{#entityName} m WHERE UPPER(m.name)=UPPER(:name)")
    Manufacturer findManufacturerByName(String name);

}
