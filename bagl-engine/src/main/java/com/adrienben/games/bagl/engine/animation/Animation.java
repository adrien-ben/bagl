package com.adrienben.games.bagl.engine.animation;

import com.adrienben.games.bagl.engine.Time;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Animation.
 * <p>
 * An animation is a set of {@link Animator}s applied to a set of targets. Those transform should be part of a
 * same hierarchy. That way that hierarchy of nodes becomes animated.
 *
 * @param <T> The type of the targets.
 * @author adrien
 */
public class Animation<T> {

    private final Map<Class<?>, List<Animator<T, ?>>> animators = new LinkedHashMap<>();

    private final float duration;
    private float currentTime = 0.0f;
    private boolean isPlaying = false;

    private Animation(final Builder<T> builder) {
        animators.putAll(builder.animators);
        duration = (float) animators.values().stream().flatMap(List::stream).mapToDouble(Animator::getEndTime).max().orElse(0);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Advance the animation timer.
     *
     * @param time The game time.
     */
    public void step(final Time time) {
        if (isPlaying) {
            updateCurrentTime(time);
            stepAnimators();
        }
    }

    private void updateCurrentTime(final Time time) {
        currentTime += time.getElapsedTime();
        if (currentTime > duration) {
            currentTime -= duration;
        }
    }

    private void stepAnimators() {
        animators.values().stream().flatMap(List::stream).forEach(animator -> animator.execute(currentTime));
    }

    /**
     * Toggle the animation.
     * <p>
     * If it is playing then its pause and id it is paused then it is resumed.
     */
    public void toggle() {
        isPlaying = !isPlaying;
    }

    /**
     * Resume the animation.
     * <p>
     * If it was already playing then is has no effect.
     */
    public void play() {
        isPlaying = true;
    }

    /**
     * Pause the animation.
     * <p>
     * It it was paused then it has no effect.
     */
    public void pause() {
        isPlaying = false;
    }

    /**
     * Stop the animation.
     * <p>
     * The animation is reset and paused. If the animation hadn't started
     * then it has no effect.
     */
    public void stop() {
        pause();
        reset();
    }

    /**
     * Reset the animation.
     * <p>
     * It the animation is playing then it keeps playing but from the beginning and if it
     * was stopped then it just reset the animation timer.
     */
    public void reset() {
        currentTime = 0.0f;
        stepAnimators();
    }

    /**
     * Animation builder.
     *
     * @param <T> The animation target type.
     */
    public static class Builder<T> {

        private final Map<Class<?>, List<Animator<T, ?>>> animators = new LinkedHashMap<>();

        private Builder() {
        }

        public Animation<T> build() {
            return new Animation<>(this);
        }

        public Builder<T> animators(final Map<Class<?>, List<Animator<T, ?>>> animators) {
            animators.forEach(this::addAnimatorList);
            return this;
        }

        private void addAnimatorList(final Class<?> animatorsClass, final List<Animator<T, ?>> animators) {
            this.animators.computeIfAbsent(animatorsClass, key -> new ArrayList<>()).addAll(animators);
        }

        public Builder<T> animator(final Class<?> animatorClass, final Animator<T, ?> animator) {
            this.animators.computeIfAbsent(animatorClass, key -> new ArrayList<>()).add(animator);
            return this;
        }
    }
}
