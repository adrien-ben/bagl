package com.adrien.games.bagl.utils;

import java.util.function.Predicate;

/**
 * Utility class for assertion
 */
public final class AssertUtils {

    /**
     * Private constructor to prevent instantiation
     */
    private AssertUtils() {
    }

    /**
     * Validate a value
     *
     * @param value     The value to test
     * @param predicate The test to perform
     * @param message   The error message
     * @return The value
     * @throws IllegalArgumentException if the test does not succeed
     */
    public static <T> T validate(final T value, final Predicate<T> predicate, final String message) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
