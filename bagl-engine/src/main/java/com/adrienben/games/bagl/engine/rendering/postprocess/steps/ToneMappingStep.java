package com.adrienben.games.bagl.engine.rendering.postprocess.steps;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.rendering.postprocess.PostProcessorStep;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.Texture2D;

/**
 * Tone mapping and gamma correction step.
 *
 * @author adrien
 */
public class ToneMappingStep extends PostProcessorStep {

    private FrameBuffer frameBuffer;
    private Shader toneMappingShader;

    public ToneMappingStep(final int xResolution, final int yResolution) {
        final var parameters = FrameBufferParameters.builder().depthStencilTextureParameters(null).colorOutputFormat(Format.RGBA8).build();
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
    public Texture2D onProcess(final Texture2D image) {
        frameBuffer.bind();
        frameBuffer.clear();
        image.bind();
        toneMappingShader.bind();

        renderQuad();

        Shader.unbind();
        image.unbind();
        frameBuffer.unbind();

        return frameBuffer.getColorTexture(0);
    }
}
