package com.adrien.games.bagl.rendering.postprocess;

import com.adrien.games.bagl.rendering.postprocess.steps.LastStep;
import com.adrien.games.bagl.rendering.texture.Texture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Image post processor
 *
 * @author adrien
 */
public class PostProcessor {

    public static final String POST_PROCESS_VERTEX_SHADER_FILE = "/shaders/post/post_process.vert";

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
    public void process(final Texture image) {
        var lastResult = image;
        for (final PostProcessorStep step : steps) {
            lastResult = step.process(lastResult);
        }
    }
}
