package com.damocles.fleet.fleetmanagementsystembackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Table(name="locations")
public class Location {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String street;
    private String city;
    private String country;
    private String postcode;
    @Column(name = "building_number")
    private String buildingNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;

}
