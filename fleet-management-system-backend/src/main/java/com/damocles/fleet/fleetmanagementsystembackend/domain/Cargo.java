package com.damocles.fleet.fleetmanagementsystembackend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Table(name="cargos")
public class Cargo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cargo_description")
    private String cargoDescription;
    @Column(name = "weight_kg")
    private BigDecimal weightKg;
    @Column(name = "volume_m3")
    private BigDecimal volumeM3;
    @Column(name = "pickup_date")
    private Instant  pickupDate;
    @Column(name = "delivery_date")
    private Instant deliveryDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transport_id")
    private Transport transport;
}
