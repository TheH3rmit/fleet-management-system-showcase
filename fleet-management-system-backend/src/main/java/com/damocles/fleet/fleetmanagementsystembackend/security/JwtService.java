package com.damocles.fleet.fleetmanagementsystembackend.security;

import com.damocles.fleet.fleetmanagementsystembackend.domain.UserRole;
import com.damocles.fleet.fleetmanagementsystembackend.repository.IAccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {
    private final String secret;
    private final long expMinutes;
    private final long refreshExpDays;
    private final IAccountRepository accountRepository;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes}") long expMinutes,
            @Value("${security.jwt.refresh-expiration:7}") long refreshExpDays,
            IAccountRepository accountRepository
    ) {
        this.secret = secret;
        this.expMinutes = expMinutes;
        this.refreshExpDays = refreshExpDays;
        this.accountRepository = accountRepository;
    }
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    private SecretKey key() {
        // secret min. 256-bit for HS256
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Builds an access token from Spring Security authorities.
    public String generateToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("roles",
                        authorities.stream()
                                .map(a -> a.getAuthority().replace("ROLE_", "")) // e.g. ROLE_ADMIN -> ADMIN
                                .toList()
                )
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expMinutes * 60)))
                .signWith(key())
                .compact();
    }
    // Builds an access token from enum roles.
    public String generateToken(String login, Set<UserRole> roles) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(login)
                .claim("roles", roles.stream().map(Enum::name).toList()) // ADMIN, DRIVER, DISPATCHER
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expMinutes, ChronoUnit.MINUTES)))
                .signWith(key())
                .compact();
    }
    // Builds a refresh token for the given subject and roles.
    public String generateRefreshToken(String subject, Collection<String> roles) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshExpDays, ChronoUnit.DAYS)))
                .signWith(key())
                .compact();
    }

    // Parses and verifies a signed token.
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
    }

    // Extracts the subject (login) from a token.
    public String getSubject(String token) { return parse(token).getPayload().getSubject(); }

    // Extracts role names from a token.
    public List<String> getRoles(String token) {
        var claims = parse(token).getPayload();
        var v = claims.get("roles");
        if (v instanceof List<?> l) return l.stream().map(String::valueOf).toList();
        return List.of();
    }

    // Checks whether a token is expired or invalid.
    public boolean isTokenExpired(String token) {
        try {
            var exp = parse(token).getPayload().getExpiration().toInstant();
            return exp.isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }
    }

    // Adds a token to the in-memory revocation list.
    public void revokeToken(String token) {
        revokedTokens.add(token);
    }

    // Checks whether a token has been revoked.
    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }

    // Validates refresh token expiration and role consistency.
    public boolean validateRefreshToken(String token) {
        try {

            // Blacklist + expiration
            if (isTokenExpired(token) || isRevoked(token)) return false;

            var claims = parse(token).getPayload();

            String username = claims.getSubject();
            List<String> tokenRoles = getRoles(token); // ADMIN

            // Load account
            var acc = accountRepository.findByLoginIgnoreCase(username).orElse(null);
            if (acc == null) return false;

            // Convert roles
            List<String> currentRoles = acc.getRoles().stream()
                    .map(Enum::name) // ADMIN
                    .toList();

            //  Same content check?
            return tokenRoles.containsAll(currentRoles)
                    && currentRoles.containsAll(tokenRoles);

        } catch (Exception e) {
            return false;
        }
    }

    // Validates a token against user identity and revocation state.
    public boolean isValid(String token, UserDetails user) {
        try {
            if (token == null || user == null) return false;
            if (isRevoked(token) || isTokenExpired(token)) return false;

            // parse() checks the signature (on bad token -> exception)
            String subject = getSubject(token);
            return subject != null && subject.equalsIgnoreCase(user.getUsername());
        } catch (Exception e) {
            return false;
        }
    }
}

