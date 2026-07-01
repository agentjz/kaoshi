package com.kaoshi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issue(AuthUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.accessTokenMinutes() * 60);
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getUsername())
                .claims(Map.of(
                        "uid", user.id(),
                        "displayName", user.displayName(),
                        "roles", user.roles(),
                        "permissions", user.permissions()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

