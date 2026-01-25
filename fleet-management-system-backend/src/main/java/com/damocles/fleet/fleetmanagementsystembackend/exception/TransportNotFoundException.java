package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class TransportNotFoundException extends NotFoundException  {
    public TransportNotFoundException(String message) {
        super(message);
    }
    public TransportNotFoundException(Long id) {
        super("Transport with ID " + id + " not found");
    }
}
