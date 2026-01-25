package com.damocles.fleet.fleetmanagementsystembackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Table(name = "login_histories")
public class LoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "logged_at")
    private Instant loggedAt;
    private String ip;
    @Column(name = "user_agent")
    private String userAgent;
    private String result;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "account_id",
            referencedColumnName = "id",
            nullable = false)
    private Account account;
}
