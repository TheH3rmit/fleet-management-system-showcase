package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class TrailerNotFoundException extends NotFoundException {
    public TrailerNotFoundException(String message) {
        super(message);
    }
    public TrailerNotFoundException(Long id) {
        super("Trailer with ID " + id + " not found");
    }
}
