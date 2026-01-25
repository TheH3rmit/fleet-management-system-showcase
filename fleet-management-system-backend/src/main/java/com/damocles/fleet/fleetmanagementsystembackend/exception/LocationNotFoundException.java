package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class LocationNotFoundException extends NotFoundException {
    public LocationNotFoundException(String message) {
        super(message);
    }
    public LocationNotFoundException(Long id) {
        super("Location with ID " + id + " not found");
    }
}
