package com.kaoshi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kaoshi.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenMinutes
) {
}

