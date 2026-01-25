package com.damocles.fleet.fleetmanagementsystembackend.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String manufacturer;
    private String model;
    @Column(name = "date_of_production")
    private LocalDate dateOfProduction;
    private Integer mileage;
    @Column(name = "fuel_type")
    private String fuelType;
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_status", nullable = false)
    private VehicleStatus vehicleStatus;
    @Column(unique = true, name = "license_plate")
    private String licensePlate;
    @Column(name = "allowed_load")
    private Integer allowedLoad;
    @Column(name = "insurance_number")
    private String insuranceNumber;


    @OneToMany(mappedBy = "vehicle")
    private List<Transport> transport = new ArrayList<>();

    @PrePersist
    public void pre() {
        if (vehicleStatus == null) vehicleStatus = VehicleStatus.ACTIVE;
    }
}
