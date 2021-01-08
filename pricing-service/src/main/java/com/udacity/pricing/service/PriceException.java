package com.udacity.pricing.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Price not found")
public class PriceException extends RuntimeException {

    public PriceException() {
        super();
    }

    public PriceException(String message) {
        super(message);
    }

    public PriceException(Long vehicleId) {
        super("Price not found for vehicleId " + vehicleId);
    }
}
