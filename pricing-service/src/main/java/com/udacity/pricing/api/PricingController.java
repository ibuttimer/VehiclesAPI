package com.udacity.pricing.api;

import com.udacity.pricing.domain.price.Price;
import com.udacity.pricing.service.PricingService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

import static com.udacity.pricing.config.Config.*;

/**
 * Implements a REST-based controller for the pricing service.
 */
@RestController
@ApiResponses(value = {
    @ApiResponse(code=400, message = "This is a bad request, please follow the API documentation for the proper request format."),
    @ApiResponse(code=500, message = "The server is down. Please make sure that the Pricing microservice is running.")
})
public class PricingController {


    private PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * Gets the price for a requested vehicle.
     * @param vehicleId ID number of the vehicle for which the price is requested
     * @return price of the vehicle, or error that it was not found.
     */
    @ApiResponses(value = {
        @ApiResponse(code=404, message = "A record could not be found matching the request, please verify the request parameters."),
    })
    @GetMapping(PRICING_GET_BY_VEHICLEID_URL)
    public Price getByVehicleId(@RequestParam Long vehicleId) {
        return pricingService.getPriceByVehicleId(vehicleId);
    }

    @GetMapping(PRICING_GET_RANDOM_URL)
    public Price getRandom(@RequestParam String currency, @RequestParam Long vehicleId) {
        return pricingService.setPrice(currency, vehicleId);
    }

    @DeleteMapping(PRICING_DELETE_BY_VEHICLEID_URL)
    public int deleteByVehicleId(@RequestParam Long vehicleId) {
        return pricingService.deleteByVehicleId(vehicleId);
    }
}
