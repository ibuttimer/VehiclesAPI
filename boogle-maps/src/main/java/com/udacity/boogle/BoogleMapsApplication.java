package com.udacity.boogle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udacity.boogle.maps.Address;
import com.udacity.boogle.service.AddressService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.validation.ConstraintViolationException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

@SpringBootApplication
public class BoogleMapsApplication {

	private static final Logger log = LoggerFactory.getLogger(BoogleMapsApplication.class);

	@Autowired
	private ResourceLoader resourceLoader;

	public static void main(String[] args) {
		log.info(
				String.format("Application starting with command-line arguments: %s.%n" +
						"To kill this application, press Ctrl + C.", Arrays.toString(args))
		);
		SpringApplication.run(BoogleMapsApplication.class, args);
	}

	public static final String PRELOAD_FILE = "preload.file";
	public static final String DEFAULT_FILE = "addresses.json";


	@ConditionalOnProperty(prefix = "job.autorun", name = "enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	CommandLineRunner initDatabase(AddressService addressService, @Value("${"+PRELOAD_FILE+":"+DEFAULT_FILE+"}") String filePath) {
		return args -> {
			// load addresses from resources
			if (!StringUtils.isBlank(filePath)) {
				Resource resource = resourceLoader.getResource(CLASSPATH_URL_PREFIX + filePath);
				ObjectMapper objectMapper = new ObjectMapper();

				try (InputStream is = resource.getInputStream()) {
					TypeReference<List<Address>> mapType = new TypeReference<>() {
					};
					List<Address> list = objectMapper.readValue(is, mapType);
					addressService.saveAllAddresses(list);

					log.info("Loaded {} addresses", addressService.count());
				} catch (ConstraintViolationException cve) {
					log.warn(cve.getMessage() + ": Addresses not saved");
					cve.getConstraintViolations()
							.forEach(v -> log.warn(v.getMessage()));
					cve.printStackTrace();
				} catch (Exception e) {
					log.warn(e.getMessage() + ": Addresses not saved");
					e.printStackTrace();
				}
			}
		};
	}

}
