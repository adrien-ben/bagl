package com.adrienben.games.bagl.engine.animation.selector;

import com.adrienben.games.bagl.engine.animation.KeyFrame;

/**
 * A start and end key frame to interpolate between.
 *
 * @param <T> The type of the value
 */
public record KeyFrameInterval<T>(KeyFrame<T> start, KeyFrame<T> end) {
}
