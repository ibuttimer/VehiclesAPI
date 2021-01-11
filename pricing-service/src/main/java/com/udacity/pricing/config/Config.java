package com.udacity.pricing.config;

import java.util.Map;

public class Config {

    private Config() {
        // non-instantiatable
    }

    // pricing related
    public static final String PRICING_GET_BY_VEHICLEID_URL = "/prices/search/findPriceByVehicleId";
    public static final String PRICING_POST_URL = "/prices";
    public static final String PRICING_GET_URL = "/prices";
    public static final String PRICING_DELETE_BY_VEHICLEID_URL = "/prices/search/deleteByVehicleId";
    public static final String PRICING_COUNT_URL = "/prices/search/countPrices";

    // maps related
    public static final String MAPS_GET_URL = "/maps";
    public static final String MAPS_DELETE_URL = MAPS_GET_URL;
    public static final String VEHICLES_GET_URL = "/vehicles";

    // vehicle related
    public static final String CARS_URL = "/cars";
    public static final String CARS_GET_BY_ID_URL = "/{id}";
    public static final String CARS_PUT_BY_ID_URL = "/{id}";
    public static final String CARS_DELETE_BY_ID_URL = "/{id}";


    public static String getUrl(String url, Map<String, Object> query) {
        StringBuilder sb = new StringBuilder(
                url.endsWith("/") ? url.substring(0, url.length() - 1) : url);
        boolean start = true;
        for (Map.Entry<String, Object> entry : query.entrySet()) {
            if (start) {
                sb.append('?');
                start = false;
            } else {
                sb.append('&');
            }
            sb.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue().toString());
        }
        return sb.toString();
    }

}
