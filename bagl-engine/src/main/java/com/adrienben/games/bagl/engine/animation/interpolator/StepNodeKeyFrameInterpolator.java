package com.adrienben.games.bagl.engine.animation.interpolator;

import com.adrienben.games.bagl.engine.animation.NodeKeyFrame;

/**
 * Step interpolator between two {@link NodeKeyFrame}.
 *
 * @author adrien
 */
public class StepNodeKeyFrameInterpolator implements NodeKeyFrameInterpolator {

    /**
     * {@inheritDoc}
     * <p>
     * Always copy {@code start} into {@code destination}.
     *
     * @see NodeKeyFrameInterpolator#interpolate(NodeKeyFrame, NodeKeyFrame, float, NodeKeyFrame)
     */
    @Override
    public void interpolate(final NodeKeyFrame start, final NodeKeyFrame end, final float interpolationValue, final NodeKeyFrame destination) {
        destination.setTranslation(start.getTranslation().orElse(null));
        destination.setRotation(start.getRotation().orElse(null));
        destination.setScale(start.getScale().orElse(null));
    }
}
