package com.damocles.fleet.fleetmanagementsystembackend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "first_name", nullable=false)
    private String firstName;
    @Column(name= "middle_name")
    private String middleName;
    @Column(name= "last_name", nullable=false)
    private String lastName;
    @Email @Column(unique = true, nullable=false)
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name= "birth_date")
    private LocalDate birthDate;

    @OneToOne(mappedBy = "user")
    private Account account;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = true)
    private Driver driver;


}
