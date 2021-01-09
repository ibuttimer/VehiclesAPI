package com.udacity.pricing.service;

import com.udacity.pricing.domain.price.Price;
import com.udacity.pricing.domain.price.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implements the pricing service to get prices for each vehicle.
 */
@Service
public class PricingService {

    @Autowired
    private PriceRepository priceRepository;

    /**
     * If a valid vehicle ID, gets the price of the vehicle from the stored array.
     * @param vehicleId ID number of the vehicle the price is requested for.
     * @return price of the requested vehicle
     * @throws PriceException vehicleID was not found
     */
    public Price getPriceByVehicleId(Long vehicleId) throws PriceException {
        Optional<Price> optionalPrice = Optional.ofNullable(priceRepository.findPriceByVehicleId(vehicleId));
        return optionalPrice.orElseThrow(() -> new PriceException(vehicleId));
    }

    /**
     * Generate the price for the specified vehicle and add to the database
     * @param currency - currency of price
     * @param vehicleId - id of vehicle
     * @return price of the requested vehicle
     */
    public Price setPrice(String currency, Long vehicleId) {
        Optional<Price> optionalPrice = Optional.ofNullable(priceRepository.findPriceByVehicleId(vehicleId));
        optionalPrice.ifPresent(p -> {
            // remove existing price
            priceRepository.deleteById(p.getId());
        });

        Price price = Price.of(currency, randomPrice(), vehicleId);
        return priceRepository.save(price);
    }

    /**
     * Delete a price for the specified vehicle
     * @param vehicleId - id of vehicle
     * @return number of affected entities
     */
    public int deleteByVehicleId(Long vehicleId) {
        return priceRepository.deleteByVehicleId(vehicleId);
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
