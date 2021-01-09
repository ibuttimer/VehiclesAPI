package com.udacity.boogle.maps;

import com.udacity.boogle.service.AddressService;
import org.springframework.web.bind.annotation.*;

import static com.udacity.boogle.config.Config.MAPS_DELETE_URL;
import static com.udacity.boogle.config.Config.MAPS_GET_URL;

@RestController
public class MapsController {

    public static final String LATITUDE_PARAM = "lat";
    public static final String LONGITUDE_PARAM = "lon";
    public static final String VEHICLE_ID_PARAM = "vehicleId";

    private AddressService addressService;

    public MapsController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping(MAPS_GET_URL)
    public Address get(@RequestParam Double lat, @RequestParam Double lon, @RequestParam Long vehicleId) {
        if (!AddressRecord.latitudeIsValid(lat) || !AddressRecord.longitudeIsValid(lon)) {
            throw new InvalidLocationException();
        }
        return addressService.getAddress(lat, lon, vehicleId);
    }

    @DeleteMapping(MAPS_DELETE_URL)
    public long delete(@RequestParam Long vehicleId) {
        return addressService.deleteAddress(vehicleId);
    }
}
