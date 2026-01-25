package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class VehicleNotFoundException extends NotFoundException {

  public VehicleNotFoundException(Long id) {
    super("Vehicle with ID " + id + " not found");
  }

  public VehicleNotFoundException(String licensePlate) {
    super("Vehicle with license plate " + licensePlate + " not found");
  }
}
