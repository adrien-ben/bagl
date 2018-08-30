package com.adrienben.games.bagl.renderer.shadow;

import com.adrienben.games.bagl.engine.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Compute the optimal view frustum splits for CSM rendering.
 *
 * @author adrien
 */
public class CSMSplitValuesComputer {

    private static final float LAST_SPLIT_VALUE = 1f;

    /**
     * Compute the splits value for CSM rendering.
     *
     * @param splitCount The number of splits.
     * @param zNear      The view frustum near plane.
     * @param zFar       The view frustum far plane.
     * @return A list containing the computed split values.
     */
    public List<Float> computeSplits(final int splitCount, final float zNear, final float zFar) {
        final var splitValues = new ArrayList<Float>();
        for (int i = 1; i < splitCount; i++) {
            splitValues.add(computeSplitValue(i, splitCount, zNear, zFar));
        }
        splitValues.add(LAST_SPLIT_VALUE);
        return splitValues;
    }

    private float computeSplitValue(final int splitIndex, final int maxSplits, final float zNear, final float zFar) {
        final var lambda = Configuration.getInstance().getShadowCascadeSplitLambda();
        final var depth = zFar - zNear;
        final var indexByMax = (float) splitIndex / maxSplits;
        final var clog = zNear * (float) (Math.pow(zFar / zNear, indexByMax));
        final var cuni = zNear + depth * indexByMax;
        final var value = lambda * clog + (1 - lambda) * cuni;
        return value / depth;
    }
}
