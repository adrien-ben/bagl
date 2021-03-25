package com.adrienben.games.bagl.core.math;

/**
 * Utility math functions.
 */
public final class MathUtils {

    private MathUtils() {
    }

    /**
     * Clamps a value between a lower and upper bounds.
     *
     * @param value The value to clamp.
     * @param min   The lower bound.
     * @param max   The upper bound.
     * @return The clamped value.
     */
    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    /**
     * Transform an angle in degrees to an angle in radians.
     */
    public static float toRadians(final float angleInDegrees) {
        return (float) Math.toRadians(angleInDegrees);
    }

    /**
     * Generate a random value between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static float random() {
        return (float) Math.random();
    }

    /**
     * Generate a random value between 0.0 (inclusive) and {@code max} (exclusive).
     */
    public static float random(final float max) {
        return random() * max;
    }

    /**
     * Generate a random value between {@code min} (inclusive) and {@code max} (exclusive).
     */
    public static float random(final float min, final float max) {
        final var delta = max - min;
        return random(delta) + min;
    }

    /**
     * Generate a random value between {@code range} min value (inclusive) and {@code range} max value (exclusive).
     */
    public static float random(final Range<Float> range) {
        return random(range.getMin(), range.getMax());
    }
}
