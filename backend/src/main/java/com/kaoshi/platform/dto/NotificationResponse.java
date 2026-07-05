package com.kaoshi.platform.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long recipientUserId,
        String title,
        String content,
        String category,
        Boolean read,
        LocalDateTime createdAt
) {
}
