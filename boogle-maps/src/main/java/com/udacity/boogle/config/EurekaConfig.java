package com.udacity.boogle.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = {"spring.cloud.service-registry.auto-registration.enabled",
                "spring.cloud.discovery.enabled",
                "eureka.client.register-with-eureka",
                "eureka.client.fetch-registry",
                "eureka.client.enabled"},
        matchIfMissing = true
)@EnableEurekaClient
public class EurekaConfig {
}
