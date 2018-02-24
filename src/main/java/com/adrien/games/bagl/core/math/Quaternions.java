package com.adrien.games.bagl.core.math;

import org.joml.Quaternionfc;
import org.joml.Vector3f;

/**
 * Utility methods and constants for joml quaternions
 *
 * @author adrien
 */
public class Quaternions {

    /**
     * Private constructor to prevent initialization
     */
    private Quaternions() {
    }

    /**
     * Retrieve the forward vector of the rotation represented by
     * a quaternion. This is equivalent of doing q*(0, 0, 1)
     *
     * @param q The quaternion to retrieve the forward vector from
     * @return A new {@link Vector3f} representing
     */
    public static Vector3f getForwardVector(final Quaternionfc q) {
        return Quaternions.getForwardVector(q, new Vector3f());
    }

    /**
     * Compute the forward vector of the rotation represented by a
     * quaternion and store the result in a given vector. This is
     * equivalent of doing q*(0, 0, 1)
     *
     * @param q      The quaternion to retrieve the forward vector from
     * @param target The vector where to store the result
     * @return target
     */
    public static Vector3f getForwardVector(final Quaternionfc q, final Vector3f target) {
        return q.transformPositiveZ(target);
    }
}
