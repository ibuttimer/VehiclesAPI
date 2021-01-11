package com.udacity.vehicles.client.prices;

import com.udacity.vehicles.client.AbstractClient;
import com.udacity.vehicles.service.ServicesService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.udacity.vehicles.config.Config.*;

/**
 * Implements a class to interface with the Pricing Client for price data.
 */
@Component
public class PriceClient extends AbstractClient {

    private static final Logger log = LoggerFactory.getLogger(PriceClient.class);

    public static final String VEHICLE_ID_PARAM = "vehicleId";
    public static final String CURRENCY_PARAM = "currency";
    public static final String PRICE_PARAM = "price";

    public enum PriceMode { EXISTING, FORCE_NEW }

    @Value("${car.price.format}")
    private String priceFormat;

    @Value("${car.price.default.currency}")
    private String defaultCurrency;

    @Value("${car.price.consult}")
    private String consultPrice;

    @Value("${pricing.service.name}")
    private String serviceName;

    private WebClient client;
    private ServicesService servicesService;

    public PriceClient(WebClient pricingWebClient, ServicesService servicesService) {
        super(pricingWebClient, servicesService);
    }

    /**
     * Get a new price for the specified vehicle
     * @param currency - currency to use
     * @param vehicleId - id of vehicle
     * @return
     */
    public String getPrice(String currency, Long vehicleId) {
        if (StringUtils.isEmpty(currency)) {
            currency = defaultCurrency;
        }
        String priceStr;
        Price price = send(requestInfo(vehicleId), HttpMethod.POST, PRICING_POST_URL, Map.of(),
                Price.of(currency, randomPrice(), vehicleId), Price.class);
        if (price == null) {
            // no response, get a new price
            priceStr = consultPrice;
        } else {
            priceStr = formatPrice(price);
        }
        return priceStr;
    }

    /**
     * Get a new price for the specified vehicle in the default currency
     * @param vehicleId - id of vehicle
     * @return
     */
    public String getPrice(Long vehicleId) {
        return getPrice(null, vehicleId);
    }

    /**
     * Get the price for the specified vehicle
     * @param vehicleId - id of vehicle
     * @return
     */
    public String getByVehicleId(Long vehicleId, PriceMode priceMode) {
        String priceStr;
        Price price = null;
        if (priceMode == PriceMode.EXISTING) {
            price = send(requestInfo(vehicleId), HttpMethod.GET, PRICING_GET_BY_VEHICLEID_URL, Map.of(
                    VEHICLE_ID_PARAM, vehicleId
            ), Price.class);
        }
        if (price == null) {
            // nothing in database or getting new price, get a new price
            deleteByVehicleId(vehicleId);
            priceStr = getPrice(vehicleId);
        } else {
            priceStr = formatPrice(price);
        }
        return priceStr;
    }

    /**
     * Delete the price for the specified vehicle
     * @param vehicleId - id of vehicle
     * @return
     */
    public long deleteByVehicleId(Long vehicleId) {
        long affected = 0L;
        Long result = send(requestInfo(vehicleId), HttpMethod.GET, PRICING_DELETE_BY_VEHICLEID_URL, Map.of(
                VEHICLE_ID_PARAM, vehicleId
        ), Long.class);
        if (result != null) {
            affected = result;
        }
        return affected;
    }

    /**
     * Get the prices count
     * @return
     */
    public long getCount() {
        return send("", HttpMethod.GET, PRICING_COUNT_URL, Map.of(), Long.class);
    }

    private String requestInfo(Long vehicleId) {
        return "vehicleId - " + vehicleId;
    }

    @Override
    protected String getServiceName() {
        return serviceName;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    private String formatPrice(Price price) {
        return String.format(priceFormat, price.getCurrency(), price.getPrice().toString());
    }

    /**
     * Gets a random price to fill in for a given vehicle ID.
     * @return random price for a vehicle
     */
    public static BigDecimal randomPrice() {
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 5))
                .multiply(new BigDecimal("5000")).setScale(2, RoundingMode.HALF_UP);
    }

}
