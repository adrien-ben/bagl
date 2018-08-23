package com.adrienben.games.bagl.engine.animation;

import com.adrienben.games.bagl.engine.Time;

import java.util.List;
import java.util.Objects;

/**
 * Represent a animation for a {@link com.adrienben.games.bagl.engine.rendering.model.Model}.
 *
 * @author adrien
 */
public class Animation {

    private final List<NodeAnimator> nodeAnimators;
    private final float duration;
    private float currentTime = 0.0f;
    private boolean isPlaying = false;

    public Animation(final List<NodeAnimator> nodeAnimators) {
        this.nodeAnimators = Objects.requireNonNull(nodeAnimators);
        this.duration = (float) nodeAnimators.stream().mapToDouble(NodeAnimator::getEndTime).max().orElse(0);
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
        nodeAnimators.forEach(nodeAnimator -> nodeAnimator.step(currentTime));
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
}
