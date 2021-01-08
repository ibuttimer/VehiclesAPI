package com.udacity.vehicles.api;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Condition;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.Details;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.service.CarService;
import com.udacity.vehicles.service.ScheduledServiceTask;
import com.udacity.vehicles.service.ServicesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static com.udacity.vehicles.config.Config.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Implements testing of the CarController class.
 */
@SpringBootTest(properties = {
        "job.autorun.enabled=false" // don't run preload of addresses into database
})
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class CarControllerTest {

    private static final Logger log = LoggerFactory.getLogger(CarControllerTest.class);

    private static final long CAR_ID = 1L;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<Car> json;

    @MockBean
    private CarService carService;

    @MockBean
    private PriceClient priceClient;

    @MockBean
    private MapsClient mapsClient;

    @MockBean
    private ScheduledServiceTask scheduledServiceTask;

    @MockBean
    private ServicesService servicesService;

    /**
     * Creates pre-requisites for testing, such as an example car.
     */
    @BeforeEach
    public void setup() {
        Car car = getCar(CAR_ID);
        given(carService.save(any())).willReturn(car);
        given(carService.findById(any())).willReturn(car);
        given(carService.list()).willReturn(Collections.singletonList(car));
    }

    /**
     * Tests for successful creation of new car in the system
     *
     * @throws Exception when car creation fails in the system
     */
    @DisplayName("Create new car")
    @Test
    public void createCar() throws Exception {
        Car car = getCar();
        String jsonStr = json.write(car).getJson();
        car.setId(CAR_ID);
        String createdJsonStr = json.write(car).getJson();

        mvc.perform(
            post(new URI(CARS_URL))
                        .content(jsonStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(createdJsonStr));
    }

    /**
     * Tests for successful update of a car in the system
     *
     * @throws Exception when car update fails in the system
     */
    @DisplayName("Update car")
    @Test
    public void updateCar() throws Exception {
        Car car = getCar(CAR_ID);
        String jsonStr = json.write(car).getJson();

        mvc.perform(
            put(getIdUri(CARS_URL + CARS_PUT_BY_ID_URL, car.getId()))
                        .content(jsonStr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonStr));
    }

    /**
     * Tests if the read operation appropriately returns a list of vehicles.
     *
     * @throws Exception if the read operation of the vehicle list fails
     */
    @DisplayName("List cars")
    @Test
    public void listCars() throws Exception {
        /**
         * TODO: Add a test to check that the `get` method works by calling
         *   the whole list of vehicles. This should utilize the car from `getCar()`
         *   below (the vehicle will be the first in the list).
         */
        Car car = getCar(CAR_ID);
        String jsonStr = json.write(car).getJson();
        jsonStr = jsonStr.substring(1, jsonStr.length() - 1);   // drop leading/trailing curly braces

        String finalJsonStr = jsonStr;
        mvc.perform(
            get(new URI(CARS_URL)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(CarResultMatcher.of(finalJsonStr, CarResultMatcher.Mode.LIST, log));
    }

    /**
     * Tests the read operation for a single car by ID.
     *
     * @throws Exception if the read operation for a single car fails
     */
    @DisplayName("Find car")
    @Test
    public void findCar() throws Exception {
        /**
         * TODO: Add a test to check that the `get` method works by calling
         *   a vehicle by ID. This should utilize the car from `getCar()` below.
         */
        Car car = getCar(CAR_ID);
        String jsonStr = json.write(car).getJson();

        mvc.perform(
                get(getIdUri(CARS_URL + CARS_GET_BY_ID_URL, car.getId())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(jsonStr));
    }

    /**
     * Tests the deletion of a single car by ID.
     *
     * @throws Exception if the delete operation of a vehicle fails
     */
    @DisplayName("Delete car")
    @Test
    public void deleteCar() throws Exception {
        /**
         * TODO: Add a test to check whether a vehicle is appropriately deleted
         *   when the `delete` method is called from the Car Controller. This
         *   should utilize the car from `getCar()` below.
         */
        Car car = getCar(CAR_ID);
        Car nonExistentCar = getCar(CAR_ID + 1);
        String jsonStr = json.write(car).getJson();

        given(carService.delete(car.getId())).willReturn(car);
        given(carService.delete(nonExistentCar.getId())).willReturn(Car.EMPTY);

        mvc.perform(
            delete(getIdUri(CARS_URL + CARS_DELETE_BY_ID_URL, car.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonStr));

        mvc.perform(
            delete(getIdUri(CARS_URL + CARS_DELETE_BY_ID_URL, nonExistentCar.getId())))
                .andExpect(status().isBadRequest());
    }

    /**
     * Creates an example Car object for use in testing.
     *
     * @return an example Car object
     */
    private Car getCar() {
        Car car = new Car();
        car.setLocation(new Location(40.730610, -73.935242));
        Details details = new Details();
        Manufacturer manufacturer = new Manufacturer(101, "Chevrolet");
        details.setManufacturer(manufacturer);
        details.setModel("Impala");
        details.setMileage(32280);
        details.setExternalColor("white");
        details.setBody("sedan");
        details.setEngine("3.6L V6");
        details.setFuelType("Gasoline");
        details.setModelYear(2018);
        details.setProductionYear(2018);
        details.setNumberOfDoors(4);
        car.setDetails(details);
        car.setCondition(Condition.USED);
        return car;
    }

    private Car getCar(Long id) {
        Car car = getCar();
        car.setId(id);
        return car;
    }

    public static URI getIdUri(String baseUrl, long id) throws URISyntaxException {
        return new URI(baseUrl.replace("{id}", Long.toString(id)));
    }
}