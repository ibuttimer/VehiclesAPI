package com.udacity.vehicles.client;

import com.udacity.vehicles.service.ServicesService;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

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
    protected <T> T send(String info, HttpMethod httpMethod, String path, Map<String, Object> query, Object body, Class<T> bodyClass) {
        T result = null;
        Function<UriBuilder, URI> uriFunction = uriBuilder -> {
            UriBuilder builder = servicesService.setBaseUrl(getServiceName(), uriBuilder)
                    .path(path);
            for (String key : query.keySet()) {
                builder.queryParam(key, query.get(key));
            }
            return builder.build();
        };
        try {
            if (ready()) {
                WebClient.RequestHeadersSpec<?> headersSpec;
                switch (httpMethod) {
                    case GET:
                    case DELETE:
                        headersSpec = (httpMethod == HttpMethod.GET ? client.get() : client.delete())
                            .uri(uriFunction);
                        break;
                    case PUT:
                    case POST:
                        if (body != null) {
                            headersSpec = (httpMethod == HttpMethod.PUT ? client.put() : client.post())
                                    .uri(uriFunction)
                                    .bodyValue(body);
                        } else {
                            headersSpec = (httpMethod == HttpMethod.PUT ? client.put() : client.post())
                                    .uri(uriFunction);
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException(httpMethod.name() + " is not supported");
                }

                result = headersSpec
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
     * Send a request to the client, given vehicle ID.
     * @param info - request info
     * @return service response
     */
    protected <T> T send(String info, HttpMethod httpMethod, String path, Map<String, Object> query, Class<T> bodyClass) {
        return send(info, httpMethod, path, query, null, bodyClass);
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
