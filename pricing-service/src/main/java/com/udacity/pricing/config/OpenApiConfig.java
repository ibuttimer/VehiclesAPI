package com.udacity.pricing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class OpenApiConfig {

    public static final String OK = "200";   // HttpStatus.OK
    public static final String CREATED = "201"; // HttpStatus.CREATED
    public static final String BAD_REQUEST = "400"; // HttpStatus.BAD_REQUEST
    public static final String NOT_FOUND = "404";   // HttpStatus.NOT_FOUND
    public static final String INTERNAL_SERVER_ERROR = "500";   // HttpStatus.INTERNAL_SERVER_ERROR


    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Pricing API")
                        .description("API of the Pricing microservice")
                        .version(appVersion)
                        .contact(new Contact().name("Ian Buttimer"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
