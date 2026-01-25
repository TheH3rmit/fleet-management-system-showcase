package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class DriverWorkLogNotFoundException extends NotFoundException {
    public DriverWorkLogNotFoundException(String message) {
        super(message);
    }
    public DriverWorkLogNotFoundException(Long id) {
        super("Drive work log with ID " + id + " not found");
    }
}

