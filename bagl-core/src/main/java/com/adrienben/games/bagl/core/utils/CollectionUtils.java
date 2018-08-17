package com.adrienben.games.bagl.core.utils;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utility methods for collections
 *
 * @author adrien
 */
public final class CollectionUtils {

    /**
     * Private constructor to prevent instantiation
     */
    private CollectionUtils() {
    }

    /**
     * Check if a collection is empty
     *
     * @param collection The collection to check
     * @return true if the collection is empty, false otherwise
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Check if a collection is not empty
     *
     * @param collection The collection to check
     * @return true if the collection is not empty, false otherwise
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return !CollectionUtils.isEmpty(collection);
    }

    /**
     * Create a list of size {@code size} and fill it will default values.
     *
     * @param listSupplier         The supplier for the list instance.
     * @param size                 The size of the list.
     * @param defaultValueSupplier The supplier for the default values.
     * @return The created list.
     */
    public static <T> List<T> createListWithDefaultValues(final Supplier<List<T>> listSupplier, final int size, final Supplier<T> defaultValueSupplier) {
        final var list = listSupplier.get();
        for (int i = 0; i < size; i++) {
            list.add(defaultValueSupplier.get());
        }
        return list;
    }
}
