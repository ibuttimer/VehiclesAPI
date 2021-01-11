package com.udacity.vehicles.service;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class ServicesService {

    private static final Logger log = LoggerFactory.getLogger(ServicesService.class);

    public enum BaseUrl { WITH, WITHOUT }

    private DiscoveryClient discoveryClient;

    private WebClientCustomizer webClientCustomizer;

    private Map<String, ServiceEntry> configured;

    private ReentrantLock mapLock;

    public ServicesService(DiscoveryClient discoveryClient, WebClientCustomizer webClientCustomizer) {
        this.discoveryClient = discoveryClient;
        this.webClientCustomizer = webClientCustomizer;
        this.configured = Maps.newHashMap();
        this.mapLock = new ReentrantLock();
    }

    public boolean serviceIsAvailable(String serviceName) {
        return getServiceInstance(serviceName).isPresent();
    }

    public URI getServiceURI(String serviceName) {
        Optional<ServiceInstance> optionalService = getServiceInstance(serviceName);
        URI uri = null;
        if (optionalService.isPresent()) {
            uri = optionalService.get().getUri();
        }
        return uri;
    }

    /**
     * Get a web client for the specified microservice
     * @param serviceName - name of microservice
     * @param baseUrl - flag to configure baseUrl
     * @return web client
     * @see <a href="https://docs.spring.io/spring-hateoas/docs/current/reference/html/#client.web-client">Configuring WebClient instances</a>
     */
    public WebClient getService(String serviceName, BaseUrl baseUrl) {
        WebClient.Builder builder = WebClient.builder();
        webClientCustomizer.customize(builder);
        WebClient webClient;
        boolean serviceConfigured = configureService(serviceName);
        if (serviceConfigured && baseUrl == BaseUrl.WITH) {
            mapLock.lock();
            try {
                ServiceEntry entry = configured.get(serviceName);
                webClient = builder
                        .baseUrl(entry.baseUri.toString())
                        .build();
            } finally {
                mapLock.unlock();
            }
        } else {
            webClient = builder.build();
        }
        return webClient;
    }

    /**
     * Configure the specified microservice
     * @param serviceName - name of microservice
     * @return <code>true</code> if configured
     */
    public boolean configureService(String serviceName) {
        boolean result;
        Optional<ServiceInstance> optionalService = getServiceInstance(serviceName);
        mapLock.lock();
        try {
            if (!configured.containsKey(serviceName)) {
                configured.put(serviceName, new ServiceEntry(serviceName, false, null));
            }
            ServiceEntry entry = configured.get(serviceName);
            optionalService.ifPresent(s -> {
                entry.configured = true;
                entry.baseUri = s.getUri();

                log.info(serviceName + " configured: " + entry.baseUri);
            });
            result = entry.configured;
        } finally {
            mapLock.unlock();
        }
        return result;
    }

    /**
     * Get an instance of the specified microservice
     * @param serviceName - name of microservice
     * @return
     */
    private Optional<ServiceInstance> getServiceInstance(String serviceName) {
        return discoveryClient.getInstances(serviceName).stream()
                .filter(s -> s.getServiceId().equalsIgnoreCase(serviceName))
                .findFirst();
    }

    /**
     * Check if all microservices are configured
     * @return <code>true</code> if all configured
     */
    public boolean allConfigured() {
        boolean result;
        mapLock.lock();
        try {
            result = configured.values().stream().mapToInt(v -> v.configured ? 1 : 0).sum() == configured.size();
        } finally {
            mapLock.unlock();
        }
        return result;
    }

    /**
     * Get the number of known microservices
     * @return
     */
    public int count() {
        int result;
        mapLock.lock();
        try {
            result = configured.size();
        } finally {
            mapLock.unlock();
        }
        return result;
    }

    /**
     * Set the base url info in the specified uri builder
     * @param serviceName - name of microservice to set url for
     * @param builder - uri builder to update
     * @return builder
     */
    public UriBuilder setBaseUrl(String serviceName, UriBuilder builder) {
        mapLock.lock();
        try {
            ServiceEntry entry = configured.get(serviceName);
            if (entry != null && entry.configured) {
                builder.scheme(entry.baseUri.getScheme())
                        .userInfo(entry.baseUri.getUserInfo())
                        .host(entry.baseUri.getHost())
                        .port(entry.baseUri.getPort());
            }
        } finally {
            mapLock.unlock();
        }
        return builder;
    }

    /**
     * Check if the specified microservices is configured
     * @param serviceName - name of microservice
     * @return <code>true</code> if configured
     */
    public boolean isConfigured(String serviceName) {
        AtomicBoolean isConfigured = new AtomicBoolean(false);
        mapLock.lock();
        try {
            configured.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(serviceName))
                    .findFirst()
                    .ifPresent(e -> isConfigured.set(e.getValue().configured));
        } finally {
            mapLock.unlock();
        }
        return isConfigured.get();
    }

    /**
     * Get a list of microservices which are not configured
     * @return
     */
    public List<String> getNotConfigured() {
        List<String> result;
        mapLock.lock();
        try {
            result = configured.entrySet().stream()
                    .filter(e -> !e.getValue().configured)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } finally {
            mapLock.unlock();
        }
        return result;
    }


    private static class ServiceEntry {
        String name;
        boolean configured;
        URI baseUri;

        public ServiceEntry(String name, boolean configured, URI baseUri) {
            this.name = name;
            this.configured = configured;
            this.baseUri = baseUri;
        }
    }
}
