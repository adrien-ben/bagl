package com.adrienben.games.bagl.engine.animation.interpolator;

import com.adrienben.games.bagl.engine.animation.NodeKeyFrame;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Linearly interpolate between two {@link NodeKeyFrame}.
 *
 * @author adrien
 */
public class LinearNodeKeyFrameInterpolator implements NodeKeyFrameInterpolator {

    private float interpolationValue;
    private NodeKeyFrame start;
    private NodeKeyFrame end;
    private NodeKeyFrame destination;

    /**
     * {@inheritDoc}
     * <p>
     * The interpolation performed is linear.
     *
     * @see NodeKeyFrameInterpolator#interpolate(NodeKeyFrame, NodeKeyFrame, float, NodeKeyFrame)
     */
    @Override
    public void interpolate(final NodeKeyFrame start, final NodeKeyFrame end, final float interpolationValue, final NodeKeyFrame destination) {
        this.interpolationValue = interpolationValue;
        this.start = start;
        this.end = end;
        this.destination = destination;
        interpolateTranslation();
        interpolateRotation();
        interpolateScale();
    }

    private void interpolateTranslation() {
        if (start.getTranslation().isPresent() && end.getTranslation().isPresent()) {
            if (!destination.getTranslation().isPresent()) {
                destination.setTranslation(new Vector3f());
            }
            start.getTranslation().get().lerp(end.getTranslation().get(), interpolationValue, destination.getTranslation().get());
        } else {
            destination.setTranslation(null);
        }
    }

    private void interpolateRotation() {
        if (start.getRotation().isPresent() && end.getRotation().isPresent()) {
            if (!destination.getRotation().isPresent()) {
                destination.setRotation(new Quaternionf());
            }
            start.getRotation().get().nlerp(end.getRotation().get(), interpolationValue, destination.getRotation().get());
        } else {
            destination.setRotation(null);
        }
    }

    private void interpolateScale() {
        if (start.getScale().isPresent() && end.getScale().isPresent()) {
            if (!destination.getScale().isPresent()) {
                destination.setScale(new Vector3f());
            }
            start.getScale().get().lerp(end.getScale().get(), interpolationValue, destination.getScale().get());
        } else {
            destination.setScale(null);
        }
    }
}
