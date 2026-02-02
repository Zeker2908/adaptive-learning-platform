package ru.zeker.authentication.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GrantAdminRequest(
        @NotNull(message = "User ID must not be null")
        UUID userId
) {}