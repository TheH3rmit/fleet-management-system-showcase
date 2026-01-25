package com.damocles.fleet.fleetmanagementsystembackend.exception;

public class LoginHistoryNotFoundException extends NotFoundException {
    public LoginHistoryNotFoundException(String message) {
        super(message);
}
    public LoginHistoryNotFoundException(Long id) {
        super("Login history record with ID " + id + " not found");
    }
}
