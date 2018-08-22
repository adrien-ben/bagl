package com.adrienben.games.bagl.engine.animation.interpolator;

import com.adrienben.games.bagl.engine.animation.NodeKeyFrame;

/**
 * Interpolate two {@link NodeKeyFrame}.
 *
 * @author adrien.
 */
public interface NodeKeyFrameInterpolator {

    /**
     * Interpolate between to {@code start} and {@code end} using {@code value} as interpolation value
     * then store the result in {@code destination}.
     *
     * @param start       The start value to interpolate from.
     * @param end         The end value to interpolate to.
     * @param value       The interpolation value. This value should be in [0.0, 1.0].
     * @param destination The {@link NodeKeyFrame} where to store the result of the interpolation.
     * @implSpec {@code start} and {@code end} must not modified. The result of the interpolation must
     * be put in {@code destination}.
     */
    void interpolate(NodeKeyFrame start, NodeKeyFrame end, float value, NodeKeyFrame destination);

}
