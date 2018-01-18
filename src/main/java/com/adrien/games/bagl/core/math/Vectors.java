package com.adrien.games.bagl.core.math;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Utility method and constants for joml vectors
 *
 * @author adrien
 */
public class Vectors {

    /**
     * Private constructor to prevent instantiation
     */
    private Vectors() {
    }

    /**
     * Check if all components of a vector are equal to 0
     *
     * @param vector The vector to check
     * @return true if all component are equal to 0
     */
    public static boolean isZero(final Vector3f vector) {
        return vector.x() == 0 && vector.y() == 0 && vector.z() == 0;
    }

    /**
     * Check if all components of a vector are equal to 0
     *
     * @param vector The vector to check
     * @return true if all component are equal to 0
     */
    public static boolean isZero(final Vector2f vector) {
        return vector.x() == 0 && vector.y() == 0;
    }

}
