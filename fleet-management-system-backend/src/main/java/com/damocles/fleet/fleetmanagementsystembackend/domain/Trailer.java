package com.damocles.fleet.fleetmanagementsystembackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Table(name = "trailers")
public class Trailer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(name = "license_plate")
    private String licensePlate;
    private BigDecimal payload;
    private BigDecimal volume;
    @Enumerated(EnumType.STRING)
    @Column(name = "trailer_status", nullable = false)
    private TrailerStatus trailerStatus;

    @OneToMany(mappedBy = "trailer", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Transport> transports = new HashSet<>();

    @PrePersist
    void prePersist() {
        if (trailerStatus == null) trailerStatus = TrailerStatus.ACTIVE;
    }
}
