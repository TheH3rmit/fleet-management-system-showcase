package com.damocles.fleet.fleetmanagementsystembackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Table(name = "driver_work_logs")
public class DriverWorkLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "break_duration")
    private Integer breakDuration;
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;
    @OneToOne
    @JoinColumn(
            name = "transport_id",
            referencedColumnName = "id",
            nullable = false,
            unique = true
    )
    private Transport transport;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type")
    private ActivityType activityType;



}

