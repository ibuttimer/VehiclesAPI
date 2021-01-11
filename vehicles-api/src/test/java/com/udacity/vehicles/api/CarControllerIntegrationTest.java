package com.udacity.vehicles.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.udacity.vehicles.domain.Condition;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import com.udacity.vehicles.domain.car.Details;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.domain.manufacturer.ManufacturerRepository;
import com.udacity.vehicles.service.CarService;
import com.udacity.vehicles.service.ManufacturerService;
import com.udacity.vehicles.service.ServicesService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.udacity.vehicles.api.CarControllerTest.getIdUri;
import static com.udacity.vehicles.config.Config.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Implements testing of the CarController class.
 */
@DisabledIfEnvironmentVariable(named = "SPRING_PROFILES_ACTIVE", matches = "test")
public class CarControllerIntegrationTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(CarControllerIntegrationTest.class);

    private static final long CAR_ID = 1L;

    @Autowired
    private CarService carService;

    @Autowired
    private ServicesService servicesService;

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private CarRepository carRepository;

    @Value("${pricing.service.name}")
    private String pricingServiceName;

    @Value("${maps.service.name}")
    private String mapsServiceName;

    @Value("${maps_db_error}")
    private String mapsDatabaseError;

    @Value("${price_db_error}")
    private String priceDatabaseError;

    static int NUM_MANUFACTURERS = 4;
    static String MANUFACTURER_NAME_TEMPLATE = "Manufacturer%d";

    static final List<Manufacturer> MANUFACTURERS = IntStream.range(0, NUM_MANUFACTURERS)
            .mapToObj(i -> Manufacturer.of(100+i, String.format(MANUFACTURER_NAME_TEMPLATE, i)))
            .collect(Collectors.toList());

    static int NUM_CARS = 4;
    static String CAR_MODEL_TEMPLATE = "Model-%d";
    static String CAR_COLOUR_TEMPLATE = "Colour-%d";
    static String CAR_BODY_TEMPLATE = "Body-%d";
    static String CAR_ENGINE_TEMPLATE = "Engine-%d";
    static String CAR_FUEL_TEMPLATE = "Fuel-%d";

    static AtomicInteger carCount = new AtomicInteger(0);

    ObjectMapper objectMapper;
    ModelMapper modelMapper;

    /**
     * Creates pre-requisites for testing, such as an example car.
     */
    @BeforeEach
    public void beforeEach() {
        assertTrue(servicesService.serviceIsAvailable(pricingServiceName), () -> "Pricing service is not available");
        assertTrue(servicesService.serviceIsAvailable(mapsServiceName), () -> "Maps service is not available");

        // check service databases are clear
        if (carCount.get() == 0) {
            assertEquals(0, carService.getVehicleCount(), mapsDatabaseError);
            assertEquals(0, carService.getPriceCount(), priceDatabaseError);
        }

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        modelMapper = new ModelMapper();

        // check all required manufacturers are in db
        int count = 0;
        if (manufacturerRepository.count() > 0) {
            count = MANUFACTURERS.stream()
                    .map(m -> manufacturerService.findById(m.getCode()))
                    .mapToInt(m -> m == null ? 0 : 1)
                    .sum();
        }
        if (count < MANUFACTURERS.size()) {
            clearRepository(manufacturerRepository);
            assertEquals(MANUFACTURERS.size(), manufacturerService.saveAll(MANUFACTURERS).size());
            IntStream.range(0, MANUFACTURERS.size())
                    .forEach(i -> {
                        Manufacturer manufacturer = manufacturerService.findById(MANUFACTURERS.get(i).getCode());
                        assertEquals(manufacturer, MANUFACTURERS.get(i));
                    });
        }
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
        List<String> elements = getElements(car, Elements.PRICE, Elements.LOCATION);

        mockMvc.perform(
            post(new URI(CARS_URL))
                    .content(objectMapper.writeValueAsString(car))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(CarResultMatcher.of(elements, CarResultMatcher.Mode.OBJECT, log));
    }

    enum Elements { DETAILS, LOCATION, CONDITION, PRICE }

    /**
     * Get the list of elements to check for
     * @param car - Car to check
     * @param excludeElements - elements to exclude
     * @return
     * @throws JsonProcessingException
     */
    List<String> getElements(Car car, Elements...excludeElements) throws JsonProcessingException {
        List<String> stringList = Lists.newArrayList();
        for (Elements elements : Elements.values()) {
            if (Arrays.stream(excludeElements)
                    .anyMatch(e -> e == elements)) {
                continue;
            }
            switch (elements) {
                case DETAILS:
                    stringList.add(objectMapper.writeValueAsString(car.getDetails()));
                    break;
                case LOCATION:
                    stringList.add(objectMapper.writeValueAsString(car.getLocation()));
                    break;
                case CONDITION:
                    stringList.add(car.getCondition().name());
                    break;
                case PRICE:
                    stringList.add(car.getPrice());
                    break;
                default:
                    // can't check created/modified time or id as don't know
                    break;
            }
        }
        return stringList;
    }


    /**
     * Tests for successful update of a car in the system
     *
     * @throws Exception when car update fails in the system
     */
    @DisplayName("Update car (excluding location)")
    @Test
    public void updateCar() throws Exception {

        Car car = carService.save(getCar());
        car.setCreatedAt(null);
        car.setModifiedAt(null);
        car.setCondition(car.getCondition() == Condition.NEW ? Condition.USED : Condition.NEW);
        Details details = car.getDetails();

        int index = MANUFACTURERS.indexOf(details.getManufacturer());
        details.setManufacturer(
                MANUFACTURERS.get(
                    index == MANUFACTURERS.size() - 1 ? 0 : index + 1
                )
        );
        details.setModel(details.getModel() + " GTX");
        details.setMileage(details.getMileage() + 5000);
        details.setExternalColor("muddy " + details.getExternalColor());
        details.setBody(details.getBody() + " with dents");
        details.setEngine("dirty " + details.getEngine());
        details.setFuelType("expensive " + details.getFuelType());
        details.setModelYear(details.getModelYear() + 20);
        details.setProductionYear(details.getProductionYear() + 20);
        details.setNumberOfDoors(details.getNumberOfDoors() + 10);

        putAndVerify(car, List.of());
    }

    /**
     * Update a car and verify it matches the expected car
     * @param car - expected car
     * @param matchers - additional checks to do
     * @param excludeElements - verification elements to exclude
     */
    void putAndVerify(Car car, List<ResultMatcher> matchers, Elements...excludeElements) throws Exception {
        List<String> elements = getElements(car, excludeElements);

        AtomicReference<ResultActions> resultActions = new AtomicReference<>(
                mockMvc.perform(
                    put(getIdUri(CARS_URL + CARS_PUT_BY_ID_URL, car.getId()))
                        .content(objectMapper.writeValueAsString(car))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(CarResultMatcher.of(elements, CarResultMatcher.Mode.OBJECT, log)));

        matchers.forEach(matcher -> {
            resultActions.getAndUpdate(ra -> {
                try {
                    ra.andExpect(matcher);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
                return ra;
            });
        });
    }

    /**
     * Tests if the read operation appropriately returns a list of vehicles.
     *
     * @throws Exception if the read operation of the vehicle list fails
     */
    @DisplayName("List cars")
    @Test
    public void listCars() throws Exception {

        // add some cars to the database
        List<Car> cars = getCars(NUM_CARS);
        assertEquals(NUM_CARS, cars.size());
        List<Car> repositoryCars = carService.saveAll(cars);
        assertEquals(repositoryCars.size(), cars.size());

        // verify added cars are in repository
        List<List<String>> elementsList = Lists.newArrayList();
        List<String> selections = Lists.newArrayList();
        IntStream.range(0, cars.size()).forEach(i -> {
            try {
                List<String> elements = getElements(cars.get(i), Elements.LOCATION);
                elementsList.add(elements);
                selections.add(".*" + cars.get(i).getDetails().getEngine() + ".*");

                String jsonStr = objectMapper.writeValueAsString(repositoryCars.get(i));

                assertEquals(elements.stream()
                        .mapToInt(s -> jsonStr.contains(s) ? 1 : 0)
                        .sum(), elements.size(), () -> "Not all elements matched for car " + i);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                fail();
            }
        });

        // verify each car is in the list
        mockMvc.perform(
            get(new URI(CARS_URL)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(HAL_JSON))
                .andExpect(mvcResult -> {
                    for (int i = 0; i < elementsList.size(); i++) {
                        try {
                            CarResultMatcher.of(elementsList.get(i), CarResultMatcher.Mode.LIST, selections.get(i), log)
                                    .match(mvcResult);
                        } catch (Exception e) {
                            e.printStackTrace();
                            fail();
                        }
                    }
                });
    }

    /**
     * Tests the read operation for a single car by ID.
     *
     * @throws Exception if the read operation for a single car fails
     */
    @DisplayName("Find car")
    @Test
    public void findCar() throws Exception {

        getAndVerifyCar(carService.save(getCar()), List.of());
    }

    /**
     * Get a car and verify it matches the expected car
     * @param car - expected car
     * @param matchers - additional checks to do
     * @param excludeElements - verification elements to exclude
     * @throws Exception
     */
    public void getAndVerifyCar(Car car, List<ResultMatcher> matchers, Elements...excludeElements) throws Exception {

        List<String> elements = getElements(car, excludeElements);

        AtomicReference<ResultActions> resultActions = new AtomicReference<>(
            mockMvc.perform(
                    get(getIdUri(CARS_URL + CARS_GET_BY_ID_URL, car.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(HAL_JSON))
                .andExpect(CarResultMatcher.of(elements, CarResultMatcher.Mode.OBJECT, log)));

        matchers.forEach(matcher -> {
            resultActions.getAndUpdate(ra -> {
                try {
                    ra.andExpect(matcher);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
                return ra;
            });
        });
    }

    /**
     * Tests the deletion of a single car by ID.
     *
     * @throws Exception if the delete operation of a vehicle fails
     */
    @DisplayName("Delete car")
    @Test
    public void deleteCar() throws Exception {
        Car car = carService.save(getCar());
        List<String> elements = getElements(car);

        mockMvc.perform(
            delete(getIdUri(CARS_URL + CARS_DELETE_BY_ID_URL, car.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(HAL_JSON))
                .andExpect(CarResultMatcher.of(elements, CarResultMatcher.Mode.OBJECT, log));

        mockMvc.perform(
            delete(getIdUri(CARS_URL + CARS_DELETE_BY_ID_URL, car.getId())))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests the if address changes when location is updated
     *
     * @throws Exception if the read operation for a single car fails
     */
    @DisplayName("Location changes when latitude/longitude changes")
    @Test
    public void locationCar() throws Exception {

        Car originalCar = carService.save(getCar());
        AtomicReference<Car> savedCar = new AtomicReference<>();

        // matcher to read value returned from server
        ResultMatcher readCarMatcher = mvcResult -> {
            String content = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(content);
            assertEquals(JsonNodeType.OBJECT, node.getNodeType());

            LinkedCar linkedCar = objectMapper.readValue(content, LinkedCar.class);
            Car car = new Car();
            modelMapper.map(linkedCar, car);
            savedCar.set(car);
        };

        getAndVerifyCar(originalCar, List.of(readCarMatcher));

        // update location
        Car toUpdate = savedCar.get();
        toUpdate.setLocation(
                new Location(toUpdate.getLocation().getLat() + 1, toUpdate.getLocation().getLon() + 1));
        putAndVerify(toUpdate, List.of(readCarMatcher), Elements.LOCATION);

        // verify get same car from read
        getAndVerifyCar(savedCar.get(), List.of());
    }

    /**
     * Tests the if price changes when input is updated
     *
     * @throws Exception if the read operation for a single car fails
     */
    @DisplayName("Price changes when input changes")
    @Test
    public void priceCar() throws Exception {

        Car originalCar = carService.save(getCar());
        AtomicReference<Car> savedCar = new AtomicReference<>();

        // matcher to read value returned from server
        ResultMatcher readCarMatcher = mvcResult -> {
            String content = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(content);
            assertEquals(JsonNodeType.OBJECT, node.getNodeType());

            LinkedCar linkedCar = objectMapper.readValue(content, LinkedCar.class);
            Car car = new Car();
            modelMapper.map(linkedCar, car);
            savedCar.set(car);
        };
        // TODO update to use jsonPath()

        getAndVerifyCar(originalCar, List.of(readCarMatcher));

        // update location
        Car toUpdate = savedCar.get();
        toUpdate.setPrice("new");
        putAndVerify(toUpdate, List.of(readCarMatcher), Elements.PRICE);

        // verify get same car from read
        getAndVerifyCar(savedCar.get(), List.of());
    }

    /**
     * Creates an example Car object for use in testing.
     *
     * @return an example Car object
     */
    private static Car getCar() {
        int index = carCount.getAndIncrement();
        Car car = new Car();
        car.setId(null);
        car.setLocation(new Location((double)index, (double)index));
        Details details = new Details();
        details.setManufacturer(MANUFACTURERS.get(index % MANUFACTURERS.size()));
        details.setModel(String.format(CAR_MODEL_TEMPLATE, index));
        details.setMileage(10_000 * index);
        details.setExternalColor(String.format(CAR_COLOUR_TEMPLATE, index));
        details.setBody(String.format(CAR_BODY_TEMPLATE, index));
        details.setEngine(String.format(CAR_ENGINE_TEMPLATE, index));
        details.setFuelType(String.format(CAR_FUEL_TEMPLATE, index));
        details.setModelYear(2010 + index);
        details.setProductionYear(2020 + index);
        details.setNumberOfDoors(index);
        car.setDetails(details);
        car.setCondition((index % 2) == 0 ? Condition.NEW : Condition.USED);
        return car;
    }

    private static List<Car> getCars(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> getCar())
                .collect(Collectors.toList());
    }

    /**
     * Utility class for mapping server response
     */
    static class LinkedCar extends Car {

        Links _links;

        public LinkedCar() {
            super();
        }

        public Links get_links() {
            return _links;
        }

        public void set_links(Links _links) {
            this._links = _links;
        }

        static class Links {
            Link self;
            Link cars;

            public Link getSelf() {
                return self;
            }

            public void setSelf(Link self) {
                this.self = self;
            }

            public Link getCars() {
                return cars;
            }

            public void setCars(Link cars) {
                this.cars = cars;
            }
        }
        static class Link {
            String href;

            public String getHref() {
                return href;
            }
            public void setHref(String href) {
                this.href = href;
            }
        }
    }
}