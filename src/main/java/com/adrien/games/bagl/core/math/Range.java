package com.adrien.games.bagl.core.math;

import java.util.Objects;

/**
 * A value with a min and max value
 *
 * @author adrien
 */
public class Range<T> {

    private final T min;
    private final T max;

    private Range(final T min, final T max) {
        this.min = Objects.requireNonNull(min);
        this.max = Objects.requireNonNull(max);
    }

    public static <T> Range from(final T min, final T max) {
        return new Range<>(min, max);
    }

    /**
     * Create a range with the min and max being equals.
     */
    public static <T> Range from(final T value) {
        return new Range<>(value, value);
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }
}
