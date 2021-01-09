package com.udacity.boogle.maps;

import com.udacity.boogle.service.AddressService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

import static com.udacity.boogle.config.Config.MAPS_DELETE_URL;
import static com.udacity.boogle.config.Config.MAPS_GET_URL;

@RestController
@ApiResponses(value = {
    @ApiResponse(code=400, message = "This is a bad request, please follow the API documentation for the proper request format."),
    @ApiResponse(code=404, message = "A record could not be found matching the request, please verify the request parameters."),
    @ApiResponse(code=500, message = "The server is down. Please make sure that the Maps microservice is running.")
})
public class MapsController {

    public static final String LATITUDE_PARAM = "lat";
    public static final String LONGITUDE_PARAM = "lon";
    public static final String VEHICLE_ID_PARAM = "vehicleId";

    private AddressService addressService;

    public MapsController(AddressService addressService) {
        this.addressService = addressService;
    }

    @ApiResponses(value = {
        @ApiResponse(code=404, message = "A record could not be found matching the request, please verify the request parameters."),
    })
    @GetMapping(MAPS_GET_URL)
    public Address get(@RequestParam Double lat, @RequestParam Double lon, @RequestParam Long vehicleId) {
        if (!AddressRecord.latitudeIsValid(lat) || !AddressRecord.longitudeIsValid(lon)) {
            throw new InvalidLocationException();
        }
        return addressService.getAddress(lat, lon, vehicleId);
    }

    @ApiResponses(value = {
        @ApiResponse(code=404, message = "A record could not be found matching the request, please verify the request parameters."),
    })
    @DeleteMapping(MAPS_DELETE_URL)
    public long delete(@RequestParam Long vehicleId) {
        return addressService.deleteAddress(vehicleId);
    }
}
