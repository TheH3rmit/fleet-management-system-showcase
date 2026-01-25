package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class DriverNotFoundException extends NotFoundException {
    public DriverNotFoundException(String message) {
        super(message);
    }
    public DriverNotFoundException(Long id) {
        super("Driver with ID " + id + " not found");
    }
}
