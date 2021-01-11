package com.udacity.vehicles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.service.ManufacturerNotFoundException;
import com.udacity.vehicles.service.ManufacturerService;
import com.udacity.vehicles.service.ServicesService;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebClientConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.COLLECTION_JSON;

/**
 * Launches a Spring Boot application for the Vehicles API,
 * initializes the car manufacturers in the database,
 * and launches web clients to communicate with maps and pricing.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableHypermediaSupport(type = { EnableHypermediaSupport.HypermediaType.HAL, COLLECTION_JSON})
public class VehiclesApiApplication {

    private static final Logger log = LoggerFactory.getLogger(VehiclesApiApplication.class);

    public static final String PRELOAD_MANUFACTURER_FILE = "preload.manufacturer.file";
    public static final String PRELOAD_CAR_FILE = "preload.car.file";
    public static final String DEFAULT_MANUFACTURER_FILE = "manufacturers.json";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ServicesService servicesService;

    @Autowired
    private Environment environment;


    public static void main(String[] args) {
        log.info(
            String.format("Application starting with command-line arguments: %s.%n" +
                            "To kill this application, press Ctrl + C.", Arrays.toString(args))
        );
        SpringApplication.run(VehiclesApiApplication.class, args);
    }

    @Order(1)
    @Bean
    CommandLineRunner profileLogger(Environment environment) {
        return args -> {
            log.info(
                    String.format("Active profiles: %s", Arrays.toString(environment.getActiveProfiles()))
            );
        };
    }

    /**
     * Initializes the cars & manufacturers available to the Vehicle API.
     * @param manufacturerService - where the manufacturer information persists.
     * @return the resources to add to the related repositories
     */
    @ConditionalOnProperty(prefix = "job.autorun", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Order(2)
    @Bean
    CommandLineRunner initManufacturerDatabase(ManufacturerService manufacturerService,
                                               @Value("${"+ PRELOAD_MANUFACTURER_FILE +":"+ DEFAULT_MANUFACTURER_FILE +"}") String filePath) {
        return args -> {
            if (!StringUtils.isBlank(filePath)) {
                // load manufacturers from resources
                Resource resource = resourceLoader.getResource(CLASSPATH_URL_PREFIX + filePath);
                ObjectMapper objectMapper = new ObjectMapper();
                List<Manufacturer> manufacturerList;

                try (InputStream is = resource.getInputStream()) {
                    TypeReference<List<Manufacturer>> mapType = new TypeReference<>() {
                    };
                    List<Manufacturer> list = objectMapper.readValue(is, mapType);
                    manufacturerList = manufacturerService.saveAll(list);
                    log.info("Database populated with {} manufacturers", manufacturerList.size());
                } catch (ConstraintViolationException cve) {
                    log.warn(cve.getMessage() + ": Manufacturers not saved");
                    cve.getConstraintViolations()
                            .forEach(v -> log.warn(v.getMessage()));
                    cve.printStackTrace();
                } catch (Exception ioe) {
                    log.warn(ioe.getMessage() + ": Manufacturers not saved");
                    ioe.printStackTrace();
                }

                // add unknown manufacturer
                manufacturerService.addUnknownManufacturer();
            }
        };
    }

    /**
     * Initializes the cars available to the Vehicle API.
     * @param manufacturerService - where the manufacturer information persists.
     * @param carRepository - where the car information persists.
     * @return the resources to add to the related repositories
     */
    @ConditionalOnProperty(prefix = "job.autorun", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Order(3)
    @Bean
    CommandLineRunner initCarDatabase(ManufacturerService manufacturerService, CarRepository carRepository,
                                      @Value("${"+ PRELOAD_CAR_FILE +":}") String filePath,
                                      @Value("${car.price.consult}") String consultPrice) {
        return args -> {
            if (!StringUtils.isBlank(filePath)) {
                // load cars from resources
                Resource resource = resourceLoader.getResource(CLASSPATH_URL_PREFIX + filePath);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                List<Manufacturer> manufacturerList = manufacturerService.list();

                try (InputStream is = resource.getInputStream()) {
                    TypeReference<List<Car>> mapType = new TypeReference<>() {
                    };
                    List<Car> list = objectMapper.readValue(is, mapType);
                    AtomicInteger count = new AtomicInteger(0);

                    for (Car car : list) {
                        car.ensureValid();

                        // get manufacturer from database
                        Manufacturer manufacturer = car.getDetails().getManufacturer();
                        boolean added = false;
                        try {
                            manufacturer = manufacturerService.findByNameOrId(manufacturer);

                            car.getDetails().setManufacturer(manufacturer);

                            // save car
                            carRepository.save(car);
                            added = true;
                            count.incrementAndGet();

                        } catch (ManufacturerNotFoundException mnf){
                            log.warn(String.format("Manufacturer '%s' not found", manufacturer));
                        }

                        if (!added) {
                            log.warn(String.format("Not adding car: %s", car));
                        }
                    }
                    log.info("Database populated with {} cars", count.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Web Client for the maps (location) API
     * @param serviceName - name of pricing microservice
     * @return created maps endpoint
     */
    @Bean(name="mapsWebClient")
    public WebClient webClientMaps(ServicesService servicesService, @Value("${maps.service.name}") String serviceName) {
        // get web client without base url as this will not be available if the service is not up
        return servicesService.getService(serviceName, ServicesService.BaseUrl.WITHOUT);
    }

    /**
     * Web Client for the pricing API
     * @param serviceName - name of pricing microservice
     * @return created pricing endpoint
     */
    @Bean(name="pricingWebClient")
    public WebClient webClientPricing(ServicesService servicesService, @Value("${pricing.service.name}") String serviceName) {
        // get web client without base url as this will not be available if the service is not up
        return servicesService.getService(serviceName, ServicesService.BaseUrl.WITHOUT);
    }

//    @Bean(name="configurer")
//    HypermediaWebClientConfigurer c() {
//        return new HypermediaWebClientConfigurer(new ObjectMapper(), List.of());
//    }

    @Autowired
    HypermediaWebClientConfigurer configurer;

    /**
     * Bean to customise WebClients to interact using hypermedia
     * @param configurer - Spring HATEOAS’s HypermediaWebClientConfigurer bean.
     * @return
     * @see <a href="https://docs.spring.io/spring-hateoas/docs/current/reference/html/#client.web-client">Configuring WebClient instances</a>
     */
    @Bean(name="webClientCustomizer")
    @Autowired
    WebClientCustomizer hypermediaWebClientCustomizer(HypermediaWebClientConfigurer configurer) {
        return configurer::registerHypermediaTypes;
    }

}
