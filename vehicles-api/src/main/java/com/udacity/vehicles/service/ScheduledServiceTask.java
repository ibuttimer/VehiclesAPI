package com.udacity.vehicles.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Task to check that all required microservices have been configured
 */
@Component
public class ScheduledServiceTask {

    @Autowired
    private ServicesService servicesService;

    @Scheduled(initialDelay = 0, fixedRate = 5000)
    public void checkServiceConfig() {
        servicesService.getNotConfigured()
                .forEach(serviceName -> {
                    servicesService.configureService(serviceName);
                });
    }
}
