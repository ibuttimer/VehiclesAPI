package com.udacity.vehicles.client;

import com.udacity.vehicles.service.ServicesService;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.util.Locale;
import java.util.Map;

/**
 * Base class to interface with a Client for data.
 */
@Component
public abstract class AbstractClient {

    protected WebClient client;
    protected ServicesService servicesService;

    public AbstractClient(WebClient pricingWebClient, ServicesService servicesService) {
        this.client = pricingWebClient;
        this.servicesService = servicesService;
    }

    protected abstract String getServiceName();

    protected abstract Logger getLogger();

    // In a real-world application we'll want to add some resilience
    // to this method with retries/CB/failover capabilities
    // We may also want to cache the results so we don't need to
    // do a request every time
    /**
     * Send a request to the client, given vehicle ID.
     * @param info - request info
     * @return service response
     */
    protected <T> T send(String info, HttpMethod httpMethod, String path, Map<String, Object> query, Class<T> bodyClass) {
        T result = null;
        try {
            if (ready()) {
                WebClient.RequestHeadersUriSpec<?> method;
                switch (httpMethod) {
                    case GET:
                        method = client.get();
                        break;
                    case PUT:
                        method = client.put();
                        break;
                    case POST:
                        method = client.post();
                        break;
                    case DELETE:
                        method = client.delete();
                        break;
                    default:
                        throw new UnsupportedOperationException(httpMethod.name() + " is not supported");
                }

                result = method
                        .uri(uriBuilder -> {
                            UriBuilder builder = servicesService.setBaseUrl(getServiceName(), uriBuilder)
                                    .path(path);
                            for (String key :
                                    query.keySet()) {
                                builder.queryParam(key, query.get(key));
                            }
                            return builder.build();
                        })
                        .retrieve()
                        .bodyToMono(bodyClass)
                        .block();
            }
        } catch (WebClientResponseException wcre) {
            getLogger().warn(wcre.getStatusText() + ": " + wcre.getMessage());
        } catch (Exception e) {
            getLogger().error("Unexpected error for " + httpMethod.name() + " on " + getServiceName().toUpperCase(Locale.ROOT)
                    + " service: {}", info, e);
        }
        return result;
    }


    /**
     * Check if service is configured
     * @return
     */
    private boolean ready() {
        boolean proceed = servicesService.isConfigured(getServiceName());
        if (!proceed) {
            proceed = servicesService.configureService(getServiceName());
        }
        if (!proceed) {
            getLogger().error(getServiceName().toUpperCase(Locale.ROOT) + " service is not currently available");
        }
        return proceed;
    }
}
