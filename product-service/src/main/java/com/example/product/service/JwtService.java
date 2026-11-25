package com.example.product.service;

import com.example.product.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/** Issues signed access tokens carrying the user's role in the "role" claim. */
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Duration expiry;

    public JwtService(JwtEncoder encoder, @Value("${app.jwt.expiry-minutes}") long expiryMinutes) {
        this.encoder = encoder;
        this.expiry = Duration.ofMinutes(expiryMinutes);
    }

    public TokenResponse issue(String username, String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("product-service")
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(expiry))
                .claim("role", role)
                .build();
        String token = encoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiry.toSeconds(), role);
    }
}
