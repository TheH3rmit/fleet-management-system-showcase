package com.damocles.fleet.fleetmanagementsystembackend.util;

import com.damocles.fleet.fleetmanagementsystembackend.domain.*;

public class TestDataFactory {

    public static User user(String email) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .build();
    }

    public static Vehicle vehicle(String plate) {
        return Vehicle.builder()
                .manufacturer("Volvo")
                .model("FH16")
                .licensePlate(plate)
                .allowedLoad(20000)
                .vehicleStatus(VehicleStatus.ACTIVE)
                .build();
    }

    public static Trailer trailer(String name) {
        return Trailer.builder()
                .name(name)
                .build();
    }

    public static Location location(String city) {
        return Location.builder()
                .city(city)
                .country("Poland")
                .postcode("00-001")
                .street("Test 1")
                .build();
    }

    public static Transport transport() {
        return Transport.builder()
                .status(TransportStatus.PLANNED)
                .build();
    }
}