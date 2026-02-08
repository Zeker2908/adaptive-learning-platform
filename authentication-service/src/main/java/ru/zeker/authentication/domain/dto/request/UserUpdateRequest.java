package ru.zeker.authentication.domain.dto.request;

import lombok.Builder;

@Builder
public record UserUpdateRequest(
        String firstName,
        String lastName
) {
}