package com.adrien.games.bagl.engine.rendering.postprocess.steps;

import com.adrien.games.bagl.core.io.ResourcePath;
import com.adrien.games.bagl.engine.rendering.postprocess.PostProcessorStep;
import com.adrien.games.bagl.opengl.shader.Shader;
import com.adrien.games.bagl.opengl.texture.Texture;

/**
 * Last step of the post processing pipeline.
 * <p>
 * This step must always be executed as the last step of the post processing pipeline.
 * This step will just render its input image in the currently bound frame buffer or
 * in the default back buffer back buffer.
 *
 * @author adrien
 */
public class LastStep extends PostProcessorStep {

    private Shader shader;

    public LastStep() {
        this.shader = buildProcessShader(ResourcePath.get("classpath:/shaders/post/post_process.frag"));
    }

    /**
     * {@inheritDoc}
     *
     * @see PostProcessorStep#onDestroy()
     */
    @Override
    public void onDestroy() {
        shader.destroy();
    }

    /**
     * Render {@code image} in the currently bound frame buffer. Or in
     * the default back buffer is no frame buffer is bound.
     * <p>
     * {@code image} will be returned as is.
     */
    @Override
    protected Texture onProcess(final Texture image) {
        shader.bind();
        image.bind();

        renderQuad();

        Texture.unbind();
        Shader.unbind();
        return image;
    }
}
