package com.adrienben.games.bagl.engine.animation;

import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.animation.interpolator.NodeKeyFrameInterpolator;

import java.util.List;
import java.util.Objects;

/**
 * Animates a node by applying interpolated {@link NodeKeyFrame} to its transform.
 *
 * @author adrien
 */
public class NodeAnimator {

    private final Transform target;
    private final List<NodeKeyFrame> nodeKeyFrames;
    private final NodeKeyFrameSelector nodeKeyFrameSelector;
    private final NodeKeyFrameInterpolator nodeKeyFrameInterpolator;
    private NodeKeyFrame currentNodeKeyFrame = new NodeKeyFrame(-1);
    private final float endTime;

    private NodeKeyFrame lastNodeKeyFrame = null;
    private NodeKeyFrame nextNodeKeyFrame = null;
    private float currentTime;

    public NodeAnimator(final Transform target, final List<NodeKeyFrame> nodeKeyFrames, final NodeKeyFrameInterpolator nodeKeyFrameInterpolator) {
        this.target = Objects.requireNonNull(target);
        this.nodeKeyFrames = Objects.requireNonNull(nodeKeyFrames);
        this.nodeKeyFrameSelector = new NodeKeyFrameSelector(nodeKeyFrames);
        this.nodeKeyFrameInterpolator = nodeKeyFrameInterpolator;
        this.endTime = nodeKeyFrames.get(nodeKeyFrames.size() - 1).getTime();
    }

    /**
     * Animate its target node's transform.
     * <p>
     * It does that by applying the interpolation between two {@link NodeKeyFrame}.
     *
     * @param currentAnimationTime The current time of the animation.
     */
    public void step(final float currentAnimationTime) {
        if (CollectionUtils.isEmpty(nodeKeyFrames)) {
            return;
        }
        currentTime = currentAnimationTime;
        updateCurrentKeyFrame();
        animateNode();
    }

    private void updateCurrentKeyFrame() {
        final var currentInterval = nodeKeyFrameSelector.selectCurrentInterval(currentTime);
        lastNodeKeyFrame = currentInterval.getFirst();
        nextNodeKeyFrame = currentInterval.getSecond();
    }

    private void animateNode() {
        final float interpolationValue = computeAndGetInterpolationValue();
        nodeKeyFrameInterpolator.interpolate(lastNodeKeyFrame, nextNodeKeyFrame, interpolationValue, currentNodeKeyFrame);
        currentNodeKeyFrame.getTranslation().ifPresent(target::setTranslation);
        currentNodeKeyFrame.getRotation().ifPresent(target::setRotation);
        currentNodeKeyFrame.getScale().ifPresent(target::setScale);
    }

    private float computeAndGetInterpolationValue() {
        if (Objects.equals(lastNodeKeyFrame, nextNodeKeyFrame)) {
            return 0;
        }
        return (currentTime - lastNodeKeyFrame.getTime()) / (nextNodeKeyFrame.getTime() - lastNodeKeyFrame.getTime());
    }

    public float getEndTime() {
        return endTime;
    }
}
