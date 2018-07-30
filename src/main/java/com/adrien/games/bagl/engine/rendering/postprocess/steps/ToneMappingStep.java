package com.adrien.games.bagl.engine.rendering.postprocess.steps;

import com.adrien.games.bagl.core.io.ResourcePath;
import com.adrien.games.bagl.engine.rendering.postprocess.PostProcessorStep;
import com.adrien.games.bagl.opengl.FrameBuffer;
import com.adrien.games.bagl.opengl.FrameBufferParameters;
import com.adrien.games.bagl.opengl.shader.Shader;
import com.adrien.games.bagl.opengl.texture.Format;
import com.adrien.games.bagl.opengl.texture.Texture;

/**
 * Tone mapping and gamma correction step.
 *
 * @author adrien
 */
public class ToneMappingStep extends PostProcessorStep {

    private FrameBuffer frameBuffer;
    private Shader toneMappingShader;

    public ToneMappingStep(final int xResolution, final int yResolution) {
        final var parameters = FrameBufferParameters.builder().hasDepthStencil(false).colorOutputFormat(Format.RGBA8).build();
        this.frameBuffer = new FrameBuffer(xResolution, yResolution, parameters);
        this.toneMappingShader = buildProcessShader(ResourcePath.get("classpath:/shaders/post/tone_mapping.frag"));
    }

    /**
     * {@inheritDoc}
     *
     * @see PostProcessorStep#onDestroy()
     */
    @Override
    public void onDestroy() {
        frameBuffer.destroy();
        toneMappingShader.destroy();
    }

    /**
     * Apply tone mapping and gamma correction on {@code image}.
     */
    @Override
    public Texture onProcess(final Texture image) {
        frameBuffer.bind();
        frameBuffer.clear();
        image.bind();
        toneMappingShader.bind();

        renderQuad();

        Shader.unbind();
        Texture.unbind();
        frameBuffer.unbind();

        return frameBuffer.getColorTexture(0);
    }
}
