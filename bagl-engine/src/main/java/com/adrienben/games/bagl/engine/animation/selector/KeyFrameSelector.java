package com.adrienben.games.bagl.engine.animation.selector;

import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.animation.KeyFrame;

import java.util.List;
import java.util.Objects;

/**
 * Select the key frames to interpolate at a given time.
 *
 * @author adrien
 */
public class KeyFrameSelector<T> {

    private final List<KeyFrame<T>> keyFrames;
    private final float startTime;
    private final float endTime;

    private float currentAnimationTime;

    public KeyFrameSelector(final List<KeyFrame<T>> keyFrames) {
        this.keyFrames = Objects.requireNonNull(keyFrames);
        this.startTime = keyFrames.get(0).time();
        this.endTime = keyFrames.get(keyFrames.size() - 1).time();
    }

    /**
     * Select the two key frames to interpolate at a given animation time value.
     *
     * @param currentAnimationTime The current animation time.
     * @return A new {@link KeyFrameInterval} whose first element is the earliest keyframe and the second is the latest.
     */
    public KeyFrameInterval<T> selectCurrentInterval(final float currentAnimationTime) {
        if (CollectionUtils.isEmpty(keyFrames)) {
            return null;
        }
        this.currentAnimationTime = currentAnimationTime;
        return computeAndGetLastAndNextKeyFrames();
    }

    private KeyFrameInterval<T> computeAndGetLastAndNextKeyFrames() {
        if (!hasStarted() || keyFrames.size() == 1) {
            final var firstNodeKeyFrame = keyFrames.get(0);
            return new KeyFrameInterval<>(firstNodeKeyFrame, firstNodeKeyFrame);
        } else if (hasEnded()) {
            final var lastKeyFrame = keyFrames.get(keyFrames.size() - 1);
            return new KeyFrameInterval<>(lastKeyFrame, lastKeyFrame);
        }
        return findCurrentNodeKeyFrameInterval();
    }

    private boolean hasStarted() {
        return currentAnimationTime >= startTime;
    }

    private boolean hasEnded() {
        return currentAnimationTime > endTime;
    }

    private KeyFrameInterval<T> findCurrentNodeKeyFrameInterval() {
        var lastNodeKeyFrame = keyFrames.get(0);
        var nextNodeKeyFrame = lastNodeKeyFrame;
        for (int i = 1; i < keyFrames.size(); i++) {
            nextNodeKeyFrame = keyFrames.get(i);
            if (isCurrentTimeInNodeKeyFrameInterval(lastNodeKeyFrame, nextNodeKeyFrame)) {
                break;
            }
            lastNodeKeyFrame = nextNodeKeyFrame;
        }
        return new KeyFrameInterval<>(lastNodeKeyFrame, nextNodeKeyFrame);
    }

    private boolean isCurrentTimeInNodeKeyFrameInterval(final KeyFrame<?> firstKeyFrame, final KeyFrame<?> secondKeyFrame) {
        return currentAnimationTime >= firstKeyFrame.time() && currentAnimationTime < secondKeyFrame.time();
    }
}
