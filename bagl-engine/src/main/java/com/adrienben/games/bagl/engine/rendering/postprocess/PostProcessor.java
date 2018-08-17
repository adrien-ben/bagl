package com.adrienben.games.bagl.engine.rendering.postprocess;

import com.adrienben.games.bagl.engine.rendering.postprocess.steps.LastStep;
import com.adrienben.games.bagl.opengl.texture.Texture2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Image post processor
 *
 * @author adrien
 */
public class PostProcessor {

    private List<PostProcessorStep> steps;

    public PostProcessor(final PostProcessorStep... steps) {
        this.steps = new ArrayList<>();
        Collections.addAll(this.steps, steps);
        this.steps.add(new LastStep());
    }

    /**
     * Release resources
     */
    public void destroy() {
        steps.forEach(PostProcessorStep::destroy);
    }

    /**
     * Apply post processing to an image
     * <p>
     * Applies bloom, gamma correction et hdr tone mapping to the image
     *
     * @param image The image to apply post processing to
     */
    public void process(final Texture2D image) {
        var lastResult = image;
        for (final PostProcessorStep step : steps) {
            lastResult = step.process(lastResult);
        }
    }
}
