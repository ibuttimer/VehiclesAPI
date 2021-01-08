package com.udacity.boogle.service;

import com.google.common.collect.Lists;
import com.udacity.boogle.maps.*;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AddressService {

    private AddressRecordRepository repository;
    private VehicleService vehicleService;

    private VehicleRepository vehicleRepository;

    public AddressService(AddressRecordRepository addressRecordRepository, VehicleService vehicleService,
                          VehicleRepository vehicleRepository) {
        this.repository = addressRecordRepository;
        this.vehicleService = vehicleService;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Create or, update if already exists, an AddressRecord
     * @param addressRecord
     * @return
     */
    public AddressRecord save(@Valid AddressRecord addressRecord) {
        AddressRecord result;
        if (addressRecord.getId() == null) {
            result = repository.save(addressRecord);
        } else{
            result = repository.findById(addressRecord.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setVehicle(addressRecord.getVehicle());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(AddressRecordNotFoundException::new);
        }
        return result;
    }

    /**
     * Either creates or updates AddressRecords, based on prior existence of manufacturer
     * @param addressRecords - AddressRecords, which can be either new or existing
     * @return the new/updated AddressRecords stored in the repository
     */
    public List<AddressRecord> saveAll(@Valid Iterable<AddressRecord> addressRecords) {
        return repository.saveAll(addressRecords);
    }

    /**
     * Save all the addresses to the database
     * @param addresses - addresses to save
     * @return
     */
    public List<AddressRecord> saveAllAddresses(@Valid Iterable<Address> addresses) {
        List<AddressRecord> addressRecords = Lists.newArrayList();
        addresses.forEach(address -> {
            AddressRecord addressRecord = new AddressRecord();
            setUnassignedLocation(addressRecord)
                    .setAddress(address);
            addressRecords.add(addressRecord);
        });
        return repository.saveAll(addressRecords);
    }

    /**
     * Get the address of a location for a vehicle. If the vehicle has not moved the current address is returned
     * otherwise a new address is provided
     * @param lat - latitude
     * @param lon - longitude
     * @param vehicleId - id of vehicle
     * @return
     */
    public Address getAddress(Double lat, Double lon, Long vehicleId) {

        AtomicReference<AddressRecord> selected = new AtomicReference<>();

        AddressRecord addressRecord = repository.findAddressRecordByVehicleId(vehicleId);
        if (addressRecord != null && addressRecord.getLat().equals(lat) && addressRecord.getLon().equals(lon)){
            // hasn't moved, return same address
            selected.set(addressRecord);
        } else {
            // randomly select an address from those which have not been allocated
            // (definitely not scalable!! there is a better way)
            List<Long> vehicleIds = vehicleRepository.findAllId();
            List<AddressRecord> allAllocated = repository.findAllByVehicleIdIn(vehicleIds);
            List<AddressRecord> allNotAllocated = repository.findAllByNotIn(allAllocated);
            if (allNotAllocated.size() == 0) {
                throw new AddressRecordNotFoundException();
            }

            deleteAllocation(vehicleId);    // delete any existing allocation

            Random generator = new Random();
            int randomIndex = generator.nextInt(allNotAllocated.size());

            Optional<AddressRecord> optionalAddressRecord =
                    repository.findById(allNotAllocated.get(randomIndex).getId());
            optionalAddressRecord.ifPresent(ar -> {
                ar.setLat(lat);
                ar.setLon(lon);
                ar.setVehicle(new Vehicle(vehicleId));
                selected.set(repository.save(ar));
            });
        }

        Address address = null;
        AddressRecord result = selected.get();
        if (result != null) {
            address = result.getAddress();
        }
        return address;
    }

    /**
     * Delete an address allocation
     * @param vehicleId - id of vehicle
     * @return
     */
    public long deleteAddress(Long vehicleId) {

        long result = deleteAllocation(vehicleId);
        if (result == 0){
            throw new AddressRecordNotFoundException();
        }
        return result;
    }

    private long deleteAllocation(Long vehicleId) {

        long result = 0;

        AddressRecord addressRecord = repository.findAddressRecordByVehicleId(vehicleId);
        if (addressRecord != null) {
            // delete the address-vehicle mapping
            setUnassignedLocation(addressRecord)
                    .setVehicle(null);
            repository.save(addressRecord);
            result = 1;
        }
        return result;
    }

    /**
     * Get current number of addresses in database
     * @return
     */
    public long count() {
        return repository.count();
    }

    private AddressRecord setUnassignedLocation(AddressRecord addressRecord) {
        addressRecord.setLat(AddressRecord.MAX_LATITUDE + 1);  // invalid latitude i.e. not allocated to vehicle
        addressRecord.setLon(AddressRecord.MAX_LONGITUDE + 1); // invalid longitude i.e. not allocated to vehicle
        return addressRecord;
    }
}
