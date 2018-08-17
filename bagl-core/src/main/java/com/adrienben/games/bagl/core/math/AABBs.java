package com.adrienben.games.bagl.core.math;

import org.joml.AABBf;
import org.joml.Vector3f;

/**
 * Utility class for {@link AABBf}.
 *
 * @author adrien
 */
public final class AABBs {

    private AABBs() {
    }

    /**
     * Create an {@link AABBf} with its min and max equal to zero.
     */
    public static AABBf createZero() {
        return new AABBf(0f, 0f, 0f, 0f, 0f, 0f);
    }

    /**
     * Compute the {@link AABBs} containing a set of points and store the result in {@code destination}.
     *
     * @param destination The {@link AABBs} where to store the result.
     * @param points      The points to compute the {@link AABBs} from.
     * @return destination.
     */
    public static AABBf computeAABBOfPoints(final AABBf destination, final Vector3f... points) {
        destination.setMin(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        destination.setMax(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (final Vector3f point : points) {
            updateAABBMinAndMax(point, destination);
        }
        return destination;
    }

    private static void updateAABBMinAndMax(final Vector3f point, final AABBf destination) {
        if (point.x() < destination.minX) {
            destination.minX = point.x();
        }
        if (point.x() > destination.maxX) {
            destination.maxX = point.x();
        }
        if (point.y() < destination.minY) {
            destination.minY = point.y();
        }
        if (point.y() > destination.maxY) {
            destination.maxY = point.y();
        }
        if (point.z() < destination.minZ) {
            destination.minZ = point.z();
        }
        if (point.z() > destination.maxZ) {
            destination.maxZ = point.z();
        }
    }
}
