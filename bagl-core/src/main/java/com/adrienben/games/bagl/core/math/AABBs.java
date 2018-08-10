package com.adrienben.games.bagl.core.math;

import org.joml.AABBf;

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

}
