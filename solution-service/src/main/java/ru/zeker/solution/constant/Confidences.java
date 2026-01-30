package ru.zeker.solution.constant;

import lombok.experimental.UtilityClass;
import ru.zeker.common.dto.task.Difficulty;

@UtilityClass
public class Confidences {

    public static final double DEFAULT_CONFIDENCE = 0.5;
    public static final double MAX_CONFIDENCE = 1.0;
    public static final double MIN_CONFIDENCE = 0.0;
    public static final String MAX_CONFIDENCE_STR = "1.0";
    public static final String MIN_CONFIDENCE_STR = "0.0";
    public static final double MAX_DIFFICULTY_FOR_PENALTY = Difficulty.HARD.getRating(); // HARD.rating
    public static final double MIN_DIFFICULTY_FOR_PENALTY = Difficulty.EASY.getRating(); // EASY.rating
    public static final double DIFFICULTY_WEIGHT_SUM = MAX_DIFFICULTY_FOR_PENALTY + MIN_DIFFICULTY_FOR_PENALTY; // = 2.0

}
