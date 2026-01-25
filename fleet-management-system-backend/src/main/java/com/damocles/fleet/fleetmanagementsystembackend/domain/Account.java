package com.damocles.fleet.fleetmanagementsystembackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "accounts", indexes = {
        @Index(name = "ix_account_login", columnList = "login", unique = true)
})

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "login", unique = true, nullable = false)
    private String login;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private AccountStatus status;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id",
            referencedColumnName = "id",
            nullable = false,
            unique = true)
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<LoginHistory> loginHistory = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles;
}



