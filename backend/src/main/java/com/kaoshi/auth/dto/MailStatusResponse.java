package com.kaoshi.auth.dto;

public record MailStatusResponse(
        boolean enabled,
        boolean configured,
        String deliveryMode,
        String from,
        String host,
        Integer port,
        String message
) {
}
