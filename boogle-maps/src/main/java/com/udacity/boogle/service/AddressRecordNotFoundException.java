package com.udacity.boogle.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "AddressRecord not found")
public class AddressRecordNotFoundException extends RuntimeException {

    public AddressRecordNotFoundException() {
    }

    public AddressRecordNotFoundException(String message) {
        super(message);
    }
}
