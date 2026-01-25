package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class CargoNotFoundException extends NotFoundException {
    public CargoNotFoundException(String message) {
        super(message);
    }
    public CargoNotFoundException(Long id) {
        super("Cargo with ID " + id + " not found");
    }
}
