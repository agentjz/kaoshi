package com.kaoshi.auth.dto;

import jakarta.validation.constraints.Size;

public record RejectRegistrationRequest(@Size(max = 255) String reason) {
}
