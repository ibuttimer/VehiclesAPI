package com.udacity.boogle.maps;

import com.udacity.boogle.service.AddressService;
import org.springframework.web.bind.annotation.*;

import static com.udacity.boogle.config.Config.MAPS_GET_URL;

@RestController
@RequestMapping(MAPS_GET_URL)
public class MapsController {

    private AddressService addressService;

    public MapsController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public Address get(@RequestParam Double lat, @RequestParam Double lon, @RequestParam Long vehicleId) {
        if (!AddressRecord.latitudeIsValid(lat) || !AddressRecord.longitudeIsValid(lon)) {
            throw new InvalidLocationException();
        }
        return addressService.getAddress(lat, lon, vehicleId);
    }

    @DeleteMapping
    public long delete(@RequestParam Long vehicleId) {
        return addressService.deleteAddress(vehicleId);
    }
}
