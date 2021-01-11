package com.udacity.vehicles.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.EXPECTATION_FAILED, reason = "Expectation failed")
public class ExpectationFailedException extends RuntimeException {

    public ExpectationFailedException() {
    }

    public ExpectationFailedException(String message) {
        super(message);
    }
}
