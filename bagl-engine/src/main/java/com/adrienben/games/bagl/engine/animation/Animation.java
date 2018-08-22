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
        updateCurrentTime(time);
        nodeAnimators.forEach(nodeAnimator -> nodeAnimator.step(currentTime));
    }

    private void updateCurrentTime(final Time time) {
        currentTime += time.getElapsedTime();
        if (currentTime > duration) {
            currentTime -= duration;
        }
    }
}
