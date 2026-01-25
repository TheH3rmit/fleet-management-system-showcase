package com.damocles.fleet.fleetmanagementsystembackend.security;

import com.damocles.fleet.fleetmanagementsystembackend.security.JwtAuthFilter;
import com.damocles.fleet.fleetmanagementsystembackend.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private UserDetailsService uds;
    private JwtAuthFilter filter;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        uds = mock(UserDetailsService.class);
        filter = new JwtAuthFilter(jwtService, uds);

        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_shouldNotAuthenticate() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
    }

    @Test
    void revokedToken_shouldSkipAuthentication() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer abc");
        when(jwtService.isRevoked("abc")).thenReturn(true);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
    }

    @Test
    void validToken_shouldAuthenticateUser() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer good");
        when(jwtService.isRevoked("good")).thenReturn(false);
        when(jwtService.isTokenExpired("good")).thenReturn(false);
        when(jwtService.getSubject("good")).thenReturn("admin");

        var userDetails = User.withUsername("admin")
                .password("x")
                .authorities("ROLE_ADMIN")
                .build();

        when(uds.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.isValid("good", userDetails)).thenReturn(true);

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        verify(chain).doFilter(req, res);
    }
}
