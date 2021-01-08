package com.udacity.boogle.maps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRecordRepository extends JpaRepository<AddressRecord, Long> {

    /**
     * Get the AddressRecords whose vehicle id is specified
     * @param id - vehicle id
     * @return
     */
    @Query("SELECT m FROM #{#entityName} m WHERE m.vehicle.id=:id")
    AddressRecord findAddressRecordByVehicleId(Long id);

    /**
     * Get a list of AddressRecords whose vehicle id is in the specified list
     * @param ids - list of vehicle ids
     * @return
     */
    @Query("SELECT m FROM #{#entityName} m WHERE m.vehicle.id IN (:ids)")
    List<AddressRecord> findAllByVehicleIdIn(List<Long> ids);

    /**
     * Get a list of AddressRecords which are not in the specified list
     * @param addressRecords - list of AddressRecord to exclude
     * @return
     */
    @Query("SELECT m FROM #{#entityName} m WHERE m NOT IN (:addressRecords)")
    List<AddressRecord> findAllByNotIn(List<AddressRecord> addressRecords);


}
