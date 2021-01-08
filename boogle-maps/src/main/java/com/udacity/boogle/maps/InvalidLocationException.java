package com.udacity.boogle.maps;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid location")
public class InvalidLocationException extends RuntimeException {

    public InvalidLocationException() {
    }

    public InvalidLocationException(String message) {
        super(message);
    }
}
