package com.udacity.vehicles.client.maps;

import com.udacity.vehicles.client.AbstractClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.service.ServicesService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static com.udacity.vehicles.config.Config.MAPS_GET_URL;

/**
 * Implements a class to interface with the Maps Client for location data.
 */
@Component
public class MapsClient extends AbstractClient  {

    private static final Logger log = LoggerFactory.getLogger(MapsClient.class);

    public static final String LATITUDE_PARAM = "lat";
    public static final String LONGITUDE_PARAM = "lon";
    public static final String VEHICLE_ID_PARAM = "vehicleId";

    @Value("${maps.service.name}")
    private String serviceName;

    @Value("${address.undetermined}")
    private String undetermined;

    private final ModelMapper mapper;

    public MapsClient(WebClient mapsWebClient, ServicesService servicesService, ModelMapper mapper) {
        super(mapsWebClient, servicesService);
        this.mapper = mapper;
    }

    /**
     * Gets an address from the Maps client, given latitude and longitude.
     * @param location An object containing "lat" and "lon" of location
     * @return An updated location including street, city, state and zip,
     *   or an exception message noting the Maps service is down
     */
    public Location getAddress(Location location, Long vehicleId) {
        Address address = send(requestInfo(location), HttpMethod.GET, MAPS_GET_URL, Map.of(
                LATITUDE_PARAM, location.getLat(),
                LONGITUDE_PARAM, location.getLon(),
                VEHICLE_ID_PARAM, vehicleId
        ), Address.class);
        if (address == null) {
            address = undeterminedAddress();
        }
        mapper.map(address, location);

        return location;
    }

    private String requestInfo(Location location) {
        return "location - (lat " + location.getLat() + ", lon " + location.getLat() + ")";
    }

    private Address undeterminedAddress() {
        return Address.of(undetermined, undetermined, undetermined, undetermined);
    }

    @Override
    protected String getServiceName() {
        return serviceName;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
