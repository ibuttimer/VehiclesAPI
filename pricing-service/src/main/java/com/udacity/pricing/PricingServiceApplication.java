package com.udacity.pricing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.pricing.domain.price.Price;
import com.udacity.pricing.domain.price.PriceRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

/**
 * Creates a Spring Boot Application to run the Pricing Service.
 * TODO: Convert the application from a REST API to a microservice.
 * See config/EurekaConfig.java
 */
@SpringBootApplication
public class PricingServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(PricingServiceApplication.class);

    @Autowired
    ResourceLoader resourceLoader;

    public static void main(String[] args) {
        log.info(
                String.format("Application starting with command-line arguments: %s.%n" +
                        "To kill this application, press Ctrl + C.", Arrays.toString(args))
        );
        SpringApplication.run(PricingServiceApplication.class, args);
    }

    public static final String PRELOAD_FILE = "preload.file";

    @Bean
    public CommandLineRunner run(PriceRepository priceRepository, @Value("${"+PRELOAD_FILE+":}") String filePath) {
        return args -> {
            if (!StringUtils.isBlank(filePath)) {
                // load prices from resources
                Resource resource = resourceLoader.getResource(CLASSPATH_URL_PREFIX + filePath);
                ObjectMapper objectMapper = new ObjectMapper();
                TypeReference<List<Price>> mapType = new TypeReference<>() {
                };

                try (InputStream is = resource.getInputStream()) {
                    List<Price> priceList = objectMapper.readValue(is, mapType);
                    priceRepository.saveAll(priceList);

                    log.info("Loaded {} prices", priceRepository.count());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
