package com.adrienben.games.bagl.core.utils;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utility class for manipulating objects.
 *
 * @author adrien
 */
public final class ObjectUtils {

    private ObjectUtils() {
    }

    /**
     * Consume {@code object} using {@code consumer} if it is not null.
     *
     * @param object   The object to consume if not null.
     * @param consumer The consumer to use.
     * @param <T>      The type of {@code object}.
     */
    public static <T> void consumeIfPresent(final T object, final Consumer<T> consumer) {
        if (Objects.nonNull(object)) {
            consumer.accept(object);
        }
    }
}
