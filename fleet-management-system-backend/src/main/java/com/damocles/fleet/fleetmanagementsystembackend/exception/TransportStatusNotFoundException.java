package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class TransportStatusNotFoundException extends NotFoundException {
    public TransportStatusNotFoundException(String message) {
        super(message);
    }
    public TransportStatusNotFoundException(Long id) {
        super("Transport status with ID " + id + " not found");
    }
}
