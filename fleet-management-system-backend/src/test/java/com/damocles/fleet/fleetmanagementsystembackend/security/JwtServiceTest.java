package com.damocles.fleet.fleetmanagementsystembackend.security;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Account;
import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import com.damocles.fleet.fleetmanagementsystembackend.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String SECRET = "12345678901234567890123456789012";

    @Mock
    private IAccountRepository accountRepo;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60, 7, accountRepo);
    }

    @Test
    void generateToken_shouldContainSubjectAndRoles() {
        String token = jwtService.generateToken("admin", Set.of(UserRole.ADMIN));

        assertEquals("admin", jwtService.getSubject(token));
        assertTrue(jwtService.getRoles(token).contains("ADMIN"));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void generateRefreshToken_shouldContainSubjectAndRoles() {
        String refresh = jwtService.generateRefreshToken("dispatcher", List.of("DISPATCHER"));

        assertEquals("dispatcher", jwtService.getSubject(refresh));
        assertEquals(List.of("DISPATCHER"), jwtService.getRoles(refresh));
        assertFalse(jwtService.isTokenExpired(refresh));
    }

    @Test
    void validateRefreshToken_shouldReturnTrue_whenRolesMatchDb() {
        String refresh = jwtService.generateRefreshToken("admin", List.of("ADMIN"));

        Account acc = new Account();
        acc.setLogin("admin");
        acc.setRoles(Set.of(UserRole.ADMIN));

        when(accountRepo.findByLoginIgnoreCase("admin")).thenReturn(Optional.of(acc));

        assertTrue(jwtService.validateRefreshToken(refresh));
    }

    @Test
    void validateRefreshToken_shouldReturnFalse_whenRolesDifferFromDb() {
        String refresh = jwtService.generateRefreshToken("admin", List.of("ADMIN"));

        Account acc = new Account();
        acc.setLogin("admin");
        acc.setRoles(Set.of(UserRole.DISPATCHER));

        when(accountRepo.findByLoginIgnoreCase("admin")).thenReturn(Optional.of(acc));

        assertFalse(jwtService.validateRefreshToken(refresh));
    }

    @Test
    void validateRefreshToken_shouldReturnFalse_whenAccountNotFound() {
        String refresh = jwtService.generateRefreshToken("ghost", List.of("ADMIN"));

        when(accountRepo.findByLoginIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertFalse(jwtService.validateRefreshToken(refresh));
    }

    @Test
    void isTokenExpired_shouldReturnTrue_forExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("test")
                .expiration(Date.from(Instant.now().minusSeconds(5)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertTrue(jwtService.isTokenExpired(expiredToken));
    }

    @Test
    void parse_shouldThrow_forBadSignature() {
        String bad = Jwts.builder()
                .subject("x")
                .claim("roles", List.of("ADMIN"))
                .expiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(Keys.hmacShaKeyFor("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(Exception.class, () -> jwtService.parse(bad));
    }

    @Test
    void revokedToken_shouldBeDetected() {
        String token = jwtService.generateToken("admin", Set.of(UserRole.ADMIN));
        jwtService.revokeToken(token);

        assertTrue(jwtService.isRevoked(token));
    }
}
