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
     * Add a post processing step to the processor.
     * <p>
     * The step is inserted just before the final step which write the the current frame buffer.
     *
     * @param step The step to add.
     */
    public void addStep(final PostProcessorStep step) {
        final var lastStep = steps.set(steps.size() - 1, step);
        steps.add(lastStep);
    }

    /**
     * Release resources
     */
    public void destroy() {
        steps.forEach(PostProcessorStep::destroy);
    }

    /**
     * Apply post processing to an image
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
