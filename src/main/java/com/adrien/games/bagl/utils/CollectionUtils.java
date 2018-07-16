package com.adrien.games.bagl.utils;

import java.util.Collection;

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
}
