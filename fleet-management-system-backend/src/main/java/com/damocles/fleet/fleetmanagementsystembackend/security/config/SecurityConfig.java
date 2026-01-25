package com.damocles.fleet.fleetmanagementsystembackend.security.config;

import com.damocles.fleet.fleetmanagementsystembackend.config.CorsProperties;
import com.damocles.fleet.fleetmanagementsystembackend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsProperties corsProperties;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // --- CORS i CSRF ---
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                // --- Stateless (no session) ---
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // --- Endpoints permissions  ---
                .authorizeHttpRequests(auth -> auth
                        // public: login + register + health + errors
                        .requestMatchers("/api/auth/**", "/error","/actuator/health","/actuator/info").permitAll()
                        // DEV: temp for testing without jwt
                        // .requestMatchers("/api/**").permitAll()

                        // Example restrictions for production
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers("/api/users/**","/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/transports/**").authenticated()
                        .anyRequest().authenticated()
                )

                // --- JWT Filter before UsernamePasswordAuthenticationFilter ---
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(corsProperties.getAllowedOrigins());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
