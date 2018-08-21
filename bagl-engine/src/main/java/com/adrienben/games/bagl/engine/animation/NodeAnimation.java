package com.adrienben.games.bagl.engine.animation;

import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.Transform;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Objects;

public class NodeAnimation {

    private final Transform target;
    private final List<KeyFrame> keyFrames;
    private final float startTime;
    private final float endTime;
    private final float totalTime;
    private KeyFrame currentKeyFrame = null;
    private KeyFrame nextKeyFrame = null;
    private float currentTime;

    public NodeAnimation(final Transform target, final List<KeyFrame> keyFrames) {
        this.target = Objects.requireNonNull(target);
        this.keyFrames = Objects.requireNonNull(keyFrames);
        this.startTime = keyFrames.get(0).getTime();
        this.endTime = keyFrames.get(keyFrames.size() - 1).getTime();
        this.totalTime = endTime - startTime;
    }

    public void step(final float currentAnimationTime) {
        if (CollectionUtils.isEmpty(keyFrames)) {
            return;
        }
        currentTime = currentAnimationTime;
        updateCurrentKeyFrame();
        animateNode();
    }

    private void updateCurrentKeyFrame() {
        if (!hasStarted()) {
            currentKeyFrame = keyFrames.get(0);
            nextKeyFrame = currentKeyFrame;
        } else if (hasEnded()) {
            currentKeyFrame = keyFrames.get(keyFrames.size() - 1);
            nextKeyFrame = currentKeyFrame;
        } else {
            currentKeyFrame = keyFrames.get(0);
            for (int i = 1; i < keyFrames.size(); i++) {
                nextKeyFrame = keyFrames.get(i);
                if (currentTime >= currentKeyFrame.getTime() && currentTime < nextKeyFrame.getTime()) {
                    return;
                }
                currentKeyFrame = nextKeyFrame;
            }
        }
    }

    private boolean hasStarted() {
        return currentTime >= startTime;
    }

    private boolean hasEnded() {
        return currentTime > endTime;
    }

    private void animateNode() {
        final float interpolationValue = computeAndGetInterpolationValue();
        translateNode(interpolationValue);
        rotateNode(interpolationValue);
        scaleNode(interpolationValue);
    }

    private float computeAndGetInterpolationValue() {
        if (currentKeyFrame == nextKeyFrame) {
            return 0;
        }
        final float currentStart = currentKeyFrame.getTime();
        final float currentEnd = nextKeyFrame.getTime();
        return (currentTime - currentStart) / (currentEnd - currentStart);
    }

    private void translateNode(final float interpolationValue) {
        if (Objects.nonNull(currentKeyFrame.getTranslation()) && Objects.nonNull(nextKeyFrame.getTranslation())) {
            target.setTranslation(interpolate(currentKeyFrame.getTranslation(), nextKeyFrame.getTranslation(), interpolationValue));
        }
    }

    private void rotateNode(final float interpolationValue) {
        if (Objects.nonNull(currentKeyFrame.getRotation()) && Objects.nonNull(nextKeyFrame.getRotation())) {
            target.setRotation(interpolate(currentKeyFrame.getRotation(), nextKeyFrame.getRotation(), interpolationValue));
        }
    }

    private void scaleNode(final float interpolationValue) {
        if (Objects.nonNull(currentKeyFrame.getScale()) && Objects.nonNull(nextKeyFrame.getScale())) {
            target.setScale(interpolate(currentKeyFrame.getScale(), nextKeyFrame.getScale(), interpolationValue));
        }
    }

    private Vector3fc interpolate(final Vector3fc start, final Vector3fc end, final float factor) {
        return start.lerp(end, factor, new Vector3f());
    }

    private Quaternionfc interpolate(final Quaternionfc start, final Quaternionfc end, final float factor) {
        return start.nlerp(end, factor, new Quaternionf());
    }

    public float getStartTime() {
        return startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public float getTotalTime() {
        return totalTime;
    }
}
