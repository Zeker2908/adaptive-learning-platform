package ru.zeker.common.dto.solution;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language {
    PYTHON(71),
    JS(63);

    private final int code;
}