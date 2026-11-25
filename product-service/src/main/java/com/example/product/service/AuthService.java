package com.example.product.service;

import com.example.product.dto.LoginRequest;
import com.example.product.dto.TokenResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Bad credentials throw AuthenticationException, which Spring Security turns into a 401. */
    public TokenResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(req.username(), req.password()));
        String role = auth.getAuthorities().iterator().next().getAuthority().replaceFirst("^ROLE_", "");
        return jwtService.issue(auth.getName(), role);
    }
}
