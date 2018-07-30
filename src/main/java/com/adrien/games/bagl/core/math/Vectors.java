package com.adrien.games.bagl.core.math;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Utility methods and constants for joml vectors
 *
 * @author adrien
 */
public class Vectors {

    public static final Vector3fc VEC3_ZERO = new Vector3f();

    public static final Vector3fc VEC3_UP = new Vector3f(0, 1, 0);

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

    /**
     * Generate a random {@link Vector3f} in a range.
     */
    public static Vector3f randomInRange(final Range<Vector3f> range) {
        final var x = MathUtils.random(range.getMin().x(), range.getMax().x());
        final var y = MathUtils.random(range.getMin().y(), range.getMax().y());
        final var z = MathUtils.random(range.getMin().z(), range.getMax().z());
        return new Vector3f(x, y, z);
    }

}
