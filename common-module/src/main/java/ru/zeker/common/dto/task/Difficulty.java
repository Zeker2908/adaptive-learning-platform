package ru.zeker.common.dto.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Difficulty {
    EASY(0.8),
    MEDIUM(1.0),
    HARD(1.2);

    private final double rating;
}