package com.adrien.games.bagl.utils;

/**
 * Utility math functions.
 */
public final class MathUtils {

    private MathUtils() {
    }

    /**
     * Clamps a value between a lower and upper bounds.
     * @param value The value to clamp.
     * @param min The lower bound.
     * @param max The upper bound.
     * @return The clamped value.
     */
    public static float clamp(float value, float min, float max) {
        return min(max, max(value, min));
    }

    /**
     * Returns the minimum between two values.
     * @param a The first value.
     * @param b The second value.
     * @return The minimum.
     */
    public static float min(float a, float b) {
        return a < b ? a : b;
    }

    /**
     * Returns the maximum between two values.
     * @param a The first value.
     * @param b The second value.
     * @return The minimum.
     */
    public static float max(float a, float b) {
        return a > b ? a : b;
    }

}
