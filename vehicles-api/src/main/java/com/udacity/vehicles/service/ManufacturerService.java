package com.udacity.vehicles.service;

import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.domain.manufacturer.ManufacturerRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
@Validated
public class ManufacturerService {

    public static final int UNKNOWN_MANUFACTURER_ID = 999;
    public static final String UNKNOWN_MANUFACTURER_NAME = "Unknown";

    @Autowired
    private Validator validator;

    private final ManufacturerRepository repository;


    public ManufacturerService(ManufacturerRepository repository) {
        this.repository = repository;
    }

    /**
     * Gathers a list of all manufacturers
     * @return a list of all manufacturers in the ManufacturerRepository
     */
    public List<Manufacturer> list() {
        return repository.findAll();
    }

    /**
     * Gets manufacturer information by ID (or throws exception if non-existent)
     * @param id the ID number of the manufacturer to gather information on
     * @return the requested manufacturer's information
     * @throws ManufacturerNotFoundException
     */
    public Manufacturer findById(Integer id) {
        Optional<Manufacturer> optionalManufacturer = repository.findById(id);
        return optionalManufacturer.orElseThrow(ManufacturerNotFoundException::new);
    }

    /**
     * Gets manufacturer information by name (or throws exception if non-existent)
     * @param name the name of the manufacturer to gather information on
     * @return the requested manufacturer's information
     * @throws ManufacturerNotFoundException
     */
    public Manufacturer findByName(String name) {
        Manufacturer manufacturer = repository.findManufacturerByName(name);
        if (manufacturer == null) {
            throw new ManufacturerNotFoundException();
        }
        return manufacturer;
    }

    /**
     * Find a manufacturer by name or id
     * @param name - manufacturer name
     * @param id - manufacturer code
     * @return
     * @throws ManufacturerNotFoundException
     */
    public Manufacturer findByNameOrId(String name, Integer id) {
        // get manufacturer from database
        AtomicBoolean added = new AtomicBoolean(false);
        Optional<Manufacturer> manufacturer = Optional.empty();

        if ((id == null || id == UNKNOWN_MANUFACTURER_ID) && !StringUtils.isBlank(name)) {
            Manufacturer repoManufacturer = repository.findManufacturerByName(name);
            if (repoManufacturer != null) {
                manufacturer = Optional.of(repoManufacturer);
            }
        } else if (id != null){
            manufacturer = repository.findById(id);
        }
        return manufacturer.orElseThrow(ManufacturerNotFoundException::new);
    }

    /**
     * Find a manufacturer by name or id
     * @param manufacturer - manufacturer object
     * @return
     * @throws ManufacturerNotFoundException
     */
    public Manufacturer findByNameOrId(Manufacturer manufacturer) {
        return findByNameOrId(manufacturer.getName(), manufacturer.getCode());
    }


    /**
     * Either creates or updates a manufacturer, based on prior existence of manufacturer
     * @param manufacturer A manufacturer object, which can be either new or existing
     * @return the new/updated manufacturer stored in the repository
     */
    public Manufacturer save(@Valid Manufacturer manufacturer) {
        return repository.save(manufacturer);
    }

    /**
     * Either creates or updates manufacturers, based on prior existence of manufacturer
     * @param manufacturers Manufacturers, which can be either new or existing
     * @return the new/updated manufacturer stored in the repository
     */
    public List<Manufacturer> saveAll(@Valid Iterable<Manufacturer> manufacturers) {
        return repository.saveAll(manufacturers);
    }

    /**
     * Deletes a given manufacturer by ID
     * @param id the ID number of the manufacturer to delete
     */
    public long delete(Integer id) {
        Optional<Manufacturer> optionalManufacturer = repository.findById(id);
        Manufacturer manufacturer = optionalManufacturer.orElseThrow(ManufacturerNotFoundException::new);

        repository.deleteById(manufacturer.getCode());
        return !repository.existsById(manufacturer.getCode()) ? 1 : 0;
    }


    public Manufacturer addUnknownManufacturer() {
        return save(new Manufacturer().ensureValid());
    }
}
