package com.udacity.boogle.maps;

import com.udacity.boogle.service.AddressService;
import com.udacity.boogle.service.VehicleService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.udacity.boogle.config.Config.*;
import static com.udacity.boogle.config.OpenApiConfig.*;

@RestController
@ApiResponses(value = {
    @ApiResponse(responseCode = BAD_REQUEST, description = "This is a bad request, please follow the API documentation for the proper request format."),
    @ApiResponse(responseCode = INTERNAL_SERVER_ERROR, description = "The server is down. Please make sure that the Maps microservice is running.")
})
public class MapsController {

    public static final String LATITUDE_PARAM = "lat";
    public static final String LONGITUDE_PARAM = "lon";
    public static final String VEHICLE_ID_PARAM = "vehicleId";

    private AddressService addressService;
    private VehicleService vehicleService;

    public MapsController(AddressService addressService, VehicleService vehicleService) {
        this.addressService = addressService;
        this.vehicleService = vehicleService;
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = NOT_FOUND, description = "A record could not be found matching the request, please verify the request parameters."),
    })
    @GetMapping(MAPS_GET_URL)
    public Address get(@RequestParam Double lat, @RequestParam Double lon, @RequestParam Long vehicleId) {
        if (!AddressRecord.latitudeIsValid(lat) || !AddressRecord.longitudeIsValid(lon)) {
            throw new InvalidLocationException();
        }
        return addressService.getAddress(lat, lon, vehicleId);
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = NOT_FOUND, description = "A record could not be found matching the request, please verify the request parameters."),
    })
    @DeleteMapping(MAPS_DELETE_URL)
    public long delete(@RequestParam Long vehicleId) {
        return addressService.deleteAddress(vehicleId);
    }

    @GetMapping(VEHICLES_GET_URL)
    public long getVehicleCount() {
        return vehicleService.count();
    }
}
