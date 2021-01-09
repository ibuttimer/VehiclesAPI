package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
@Validated
public class CarService {

    public static final String UNKNOWN_BODY = "Unknown body";
    public static final String UNKNOWN_MODEL = "Unknown model";

    private final CarRepository repository;
    private final PriceClient pricing;
    private final MapsClient maps;
    private final ManufacturerService manufacturerService;


    public CarService(CarRepository repository, PriceClient pricing, MapsClient maps,
                      ManufacturerService manufacturerService) {
        /**
         * TODO: Add the Maps and Pricing Web Clients you create
         *   in `VehiclesApiApplication` as arguments and set them here.
         */
        this.repository = repository;
        this.pricing = pricing;
        this.maps = maps;
        this.manufacturerService = manufacturerService;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        List<Car> cars = repository.findAll();
        cars.forEach(c -> setPriceAndLocation(c, PriceClient.PriceMode.EXISTING));
        return cars;
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         *   Remove the below code as part of your implementation.
         */
        Optional<Car> optionalCar = repository.findById(id);
        Car car = optionalCar.orElseThrow(CarNotFoundException::new);
        return setPriceAndLocation(car, PriceClient.PriceMode.EXISTING);
    }

    private Car setPriceAndLocation(Car car, PriceClient.PriceMode priceMode) {
        /**
         * TODO: Use the Pricing Web client you create in `VehiclesApiApplication`
         *   to get the price based on the `id` input'
         * TODO: Set the price of the car
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */
        car.setPrice(
                pricing.getByVehicleId(car.getId(), priceMode));

        /**
         * TODO: Use the Maps Web client you create in `VehiclesApiApplication`
         *   to get the address for the vehicle. You should access the location
         *   from the car object and feed it to the Maps service.
         * TODO: Set the location of the vehicle, including the address information
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */
        car.setLocation(
                maps.getAddress(car.getLocation(), car.getId()));

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(@Valid Car car) {
        Car result;
        AtomicReference<PriceClient.PriceMode> priceMode = new AtomicReference<>(PriceClient.PriceMode.FORCE_NEW);
        if (car.getId() == null) {
            Manufacturer manufacturer = car.getDetails().getManufacturer();
            car.getDetails().setManufacturer(
                    manufacturerService.findByNameOrId(manufacturer.getName(), manufacturer.getCode())
            );
            result = repository.save(car);
        } else{
            result = repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        carToBeUpdated.setCondition(car.getCondition());

                        String price = car.getPrice();
                        if (StringUtils.isEmpty(price) || carToBeUpdated.getPrice().equals(price)) {
                            // price hasn't changed in input so no change
                            priceMode.set(PriceClient.PriceMode.EXISTING);
                        }
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }
        return setPriceAndLocation(result, priceMode.get());
    }

    /**
     * Either creates or updates vehicles, based on prior existence of car
     * @param cars A list of car objects, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public List<Car> saveAll(@Valid List<Car> cars) {
        return cars.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     * @return number of affect db entries
     */
    public Car delete(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists.
         *   If it does not exist, throw a CarNotFoundException
         */
        Optional<Car> optionalCar = repository.findById(id);
        Car car = optionalCar.orElseThrow(CarNotFoundException::new);

        /**
         * TODO: Delete the car from the repository.
         * Pricing & map services maintains records for individual vehicles, so those records are deleted as well.
         */
        id = car.getId();
        repository.deleteById(id);
        pricing.deleteByVehicleId(id);
        maps.delete(id);
        if (repository.findById(id).isPresent()) {
            car = Car.EMPTY;
        }
        return car;
    }
}
