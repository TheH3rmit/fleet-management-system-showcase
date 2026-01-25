package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class TransportNotActiveException extends RuntimeException {
    public TransportNotActiveException(Long id) {
        super("Transport " + id + " is not active.");
    }
}
