package com.deptflow.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Issues self-signed JWTs carrying the user id and optional institution id. */
@Component
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final long expirationMinutes;

    public JwtTokenService(JwtEncoder encoder,
                           @Value("${deptflow.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.encoder = encoder;
        this.expirationMinutes = expirationMinutes;
    }

    public String issue(UUID userId, String username, UUID institutionId) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .claim("username", username);
        if (institutionId != null) {
            claims.claim("institution_id", institutionId.toString());
        }
        return encoder.encode(JwtEncoderParameters.from(claims.build())).getTokenValue();
    }
}
