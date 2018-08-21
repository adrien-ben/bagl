package com.adrienben.games.bagl.engine.animation;

import com.adrienben.games.bagl.engine.Time;

import java.util.List;
import java.util.Objects;

public class Animation {

    private final List<NodeAnimation> nodeAnimations;
    private final float duration;
    private float currentTime = 0.0f;

    public Animation(final List<NodeAnimation> nodeAnimations) {
        this.nodeAnimations = Objects.requireNonNull(nodeAnimations);
        this.duration = (float) nodeAnimations.stream().mapToDouble(NodeAnimation::getEndTime).max().orElse(0);
    }

    public void step(final Time time) {
        updateCurrentTime(time);
        nodeAnimations.forEach(nodeAnimation -> nodeAnimation.step(currentTime));
    }

    private void updateCurrentTime(final Time time) {
        currentTime += time.getElapsedTime();
        if (currentTime > duration) {
            currentTime -= duration;
        }
    }

    public List<NodeAnimation> getNodeAnimations() {
        return nodeAnimations;
    }
}
