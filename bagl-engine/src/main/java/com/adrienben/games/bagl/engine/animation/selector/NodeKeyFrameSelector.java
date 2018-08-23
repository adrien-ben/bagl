package com.adrienben.games.bagl.engine.animation.selector;

import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.core.utils.Tuple2;
import com.adrienben.games.bagl.engine.animation.NodeKeyFrame;

import java.util.List;
import java.util.Objects;

/**
 * Select the key frames to interpolate at a given time.
 *
 * @author adrien
 */
public class NodeKeyFrameSelector {

    private final List<NodeKeyFrame> nodeKeyFrames;
    private final float startTime;
    private final float endTime;

    private float currentAnimationTime;

    public NodeKeyFrameSelector(final List<NodeKeyFrame> nodeKeyFrames) {
        this.nodeKeyFrames = Objects.requireNonNull(nodeKeyFrames);
        this.startTime = nodeKeyFrames.get(0).getTime();
        this.endTime = nodeKeyFrames.get(nodeKeyFrames.size() - 1).getTime();
    }

    /**
     * Select the two key frames to interpolate at a given animation time value.
     *
     * @param currentAnimationTime The current animation time.
     * @return A new {@link Tuple2} whose first element is the earliest keyframe and the second is the latest.
     */
    public Tuple2<NodeKeyFrame, NodeKeyFrame> selectCurrentInterval(final float currentAnimationTime) {
        if (CollectionUtils.isEmpty(nodeKeyFrames)) {
            return null;
        }
        this.currentAnimationTime = currentAnimationTime;
        return computeAndGetLastAndNextKeyFrames();
    }

    private Tuple2<NodeKeyFrame, NodeKeyFrame> computeAndGetLastAndNextKeyFrames() {
        if (!hasStarted() || nodeKeyFrames.size() == 1) {
            final var firstNodeKeyFrame = nodeKeyFrames.get(0);
            return new Tuple2<>(firstNodeKeyFrame, firstNodeKeyFrame);
        } else if (hasEnded()) {
            final var lastKeyFrame = nodeKeyFrames.get(nodeKeyFrames.size() - 1);
            return new Tuple2<>(lastKeyFrame, lastKeyFrame);
        }
        return findCurrentNodeKeyFrameInterval();
    }

    private boolean hasStarted() {
        return currentAnimationTime >= startTime;
    }

    private boolean hasEnded() {
        return currentAnimationTime > endTime;
    }

    private Tuple2<NodeKeyFrame, NodeKeyFrame> findCurrentNodeKeyFrameInterval() {
        var lastNodeKeyFrame = nodeKeyFrames.get(0);
        var nextNodeKeyFrame = lastNodeKeyFrame;
        for (int i = 1; i < nodeKeyFrames.size(); i++) {
            nextNodeKeyFrame = nodeKeyFrames.get(i);
            if (isCurrentTimeInNodeKeyFrameInterval(lastNodeKeyFrame, nextNodeKeyFrame)) {
                break;
            }
            lastNodeKeyFrame = nextNodeKeyFrame;
        }
        return new Tuple2<>(lastNodeKeyFrame, nextNodeKeyFrame);
    }

    private boolean isCurrentTimeInNodeKeyFrameInterval(final NodeKeyFrame firstNodeKeyFrame, final NodeKeyFrame secondNodeKeyFrame) {
        return currentAnimationTime >= firstNodeKeyFrame.getTime() && currentAnimationTime < secondNodeKeyFrame.getTime();
    }
}
