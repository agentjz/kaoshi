package com.kaoshi.auth.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kaoshi.mail")
public record MailProperties(
        boolean enabled,
        DeliveryMode deliveryMode,
        String from,
        boolean exposeDebugCode
) {
    public enum DeliveryMode {
        SMTP,
        LOG
    }
}
