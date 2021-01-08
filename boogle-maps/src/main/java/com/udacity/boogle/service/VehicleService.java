package com.udacity.boogle.service;

import com.udacity.boogle.maps.Vehicle;
import com.udacity.boogle.maps.VehicleRepository;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;

@Service
public class VehicleService {

    private VehicleRepository repository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.repository = vehicleRepository;
    }

    public Vehicle save(@Valid Vehicle vehicle) {
        return repository.save(vehicle);
    }

    public List<Vehicle> findAll() {
        return repository.findAll();
    }

    /**
     * Get a list of all vehicle ids
     * @return
     */
    public List<Long> findAllId() {
        return repository.findAllId();
    }

}
