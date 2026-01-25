package com.damocles.fleet.fleetmanagementsystembackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Table(name = "transports")
public class Transport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contractual_due_at")
    private Instant contractualDueAt;
    @Column(name = "planned_start_at")
    private Instant plannedStartAt;
    @Column(name = "planned_end_at")
    private Instant plannedEndAt;
    @Column(name = "actual_start_at")
    private Instant actualStartAt;
    @Column(name = "actual_end_at")
    private Instant actualEndAt;
    @Column(name = "planned_distance_km")
    private BigDecimal plannedDistanceKm;
    @Column(name = "actual_distance_km")
    private BigDecimal actualDistanceKm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by",
            referencedColumnName = "id",
            nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "trailer_id",
            referencedColumnName = "id")
    private Trailer trailer;

    @ManyToOne
    @JoinColumn(name = "vehicle_id",
            referencedColumnName = "id",
            nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransportStatus status;

    @OneToMany(mappedBy = "transport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cargo> cargos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pickup_address_id",
            nullable = false)
    private Location pickupLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_address_id",
            nullable = false)
    private Location deliveryLocation;

    @ManyToOne
    @JoinColumn(name="driver_id")
    private Driver driver;

    @OneToMany(mappedBy = "transport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatusHistory> statusHistories = new ArrayList<>();


}
