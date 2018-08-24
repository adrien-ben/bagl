package com.adrienben.games.bagl.engine.animation;

import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.animation.selector.KeyFrameSelector;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Animators are responsible for modifying the state of its target over time by selecting the proper {@link KeyFrame}s
 * to interpolate and updating the target with the interpolated data.
 * <p>
 * You must use the {@link Builder} to instantiate animators. You will need to provide a target, a list of
 * {@link KeyFrame} an {@link Interpolator} and a {@link TargetUpdater}.
 *
 * @param <T> The type of the target.
 * @param <U> The type of the data that belong to the target and which will be updated over time.
 */
public class Animator<T, U> {

    private final T target;
    private final List<KeyFrame<U>> keyFrames;
    private final KeyFrameSelector<U> keyFrameSelector;
    private final Interpolator<U> interpolator;
    private final TargetUpdater<T, U> targetUpdater;
    private final float endTime;

    private final U currentValue;
    private KeyFrame<U> lastKeyFrame = null;
    private KeyFrame<U> nextKeyFrame = null;
    private float currentTime;

    private Animator(final Builder<T, U> builder) {
        this.target = Objects.requireNonNull(builder.target);
        this.keyFrames = Objects.requireNonNull(builder.keyFrames);
        this.keyFrameSelector = new KeyFrameSelector<>(keyFrames);
        this.interpolator = Objects.requireNonNull(builder.interpolator);
        this.targetUpdater = Objects.requireNonNull(builder.targetUpdater);
        this.endTime = keyFrames.get(keyFrames.size() - 1).getTime();
        this.currentValue = Objects.requireNonNull(builder.currentValueSupplier).get();
    }

    /**
     * Create an {@link Builder}.
     *
     * @param <T> The type of the target of the animator.
     * @param <U> The type of the data that belong to the target and which will be updated over time.
     * @return a new builder.
     */
    public static <T, U> Builder<T, U> builder() {
        return new Builder<>();
    }

    /**
     * Execute the animator.
     * <p>
     * First the proper {@link KeyFrame}s to interpolate are selected. Then the {@link Interpolator} is used
     * to interpolate those keyframe"s data. The target is then updated with the interpolated data.
     *
     * @param currentAnimationTime The current time of the application.
     */
    public void execute(final float currentAnimationTime) {
        if (CollectionUtils.isEmpty(keyFrames)) {
            return;
        }
        currentTime = currentAnimationTime;
        updateCurrentKeyFrame();
        animateNode();
    }

    private void updateCurrentKeyFrame() {
        final var currentInterval = keyFrameSelector.selectCurrentInterval(currentTime);
        lastKeyFrame = currentInterval.getFirst();
        nextKeyFrame = currentInterval.getSecond();
    }

    private void animateNode() {
        final float interpolationValue = computeAndGetInterpolationValue();
        interpolator.interpolate(lastKeyFrame.getValue(), nextKeyFrame.getValue(), interpolationValue, currentValue);
        targetUpdater.update(target, currentValue);
    }

    private float computeAndGetInterpolationValue() {
        if (Objects.equals(lastKeyFrame, nextKeyFrame)) {
            return 0;
        }
        return (currentTime - lastKeyFrame.getTime()) / (nextKeyFrame.getTime() - lastKeyFrame.getTime());
    }

    public float getEndTime() {
        return endTime;
    }

    /**
     * The animator builder.
     *
     * @param <T> The type of the target of the animator.
     * @param <U> The type of the data that belong to the target and which will be updated over time.
     */
    public static class Builder<T, U> {

        private T target;
        private List<KeyFrame<U>> keyFrames;
        private Interpolator<U> interpolator;
        private TargetUpdater<T, U> targetUpdater;
        private Supplier<U> currentValueSupplier;

        private Builder() {
        }

        public Animator<T, U> build() {
            return new Animator<>(this);
        }

        public Builder<T, U> target(final T target) {
            this.target = target;
            return this;
        }

        public Builder<T, U> keyFrames(final List<KeyFrame<U>> keyFrames) {
            this.keyFrames = keyFrames;
            return this;
        }

        public Builder<T, U> interpolator(final Interpolator<U> interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        public Builder<T, U> targetUpdater(final TargetUpdater<T, U> targetUpdater) {
            this.targetUpdater = targetUpdater;
            return this;
        }

        public Builder<T, U> currentValueSupplier(final Supplier<U> currentValueSupplier) {
            this.currentValueSupplier = currentValueSupplier;
            return this;
        }
    }
}
