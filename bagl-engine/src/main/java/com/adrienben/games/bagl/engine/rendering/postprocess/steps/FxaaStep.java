package com.adrienben.games.bagl.engine.rendering.postprocess.steps;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.rendering.postprocess.PostProcessorStep;
import com.adrienben.games.bagl.engine.rendering.postprocess.fxaa.FxaaPresets;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import org.joml.Vector2f;

/**
 * Fxaa post processing step.
 *
 * @author adrien
 */
public class FxaaStep extends PostProcessorStep {

    private final FrameBuffer frameBuffer;
    private final Shader fxaaShader;

    public FxaaStep(final int xResolution, final int yResolution, final FxaaPresets fxaaQuality) {
        final var parameters = FrameBufferParameters.builder().hasDepthStencil(false).colorOutputFormat(Format.RGBA8).build();
        frameBuffer = new FrameBuffer(xResolution, yResolution, parameters);
        fxaaShader = buildProcessShader(ResourcePath.get("classpath:/shaders/post/fxaa.frag"))
                .bind()
                .setUniform("fxaaQualityRcpFrame", new Vector2f(1f / xResolution, 1f / yResolution))
                .setUniform("fxaaQualitySubpix", fxaaQuality.getFxaaQualitySubpix())
                .setUniform("fxaaQualityEdgeThreshold", fxaaQuality.getFxaaQualityEdgeThreshold())
                .setUniform("fxaaQualityEdgeThresholdMin", fxaaQuality.getFxaaQualityEdgeThresholdMin());
    }

    /**
     * {@inheritDoc}
     *
     * @see PostProcessorStep#onDestroy()
     */
    @Override
    public void onDestroy() {
        frameBuffer.destroy();
        fxaaShader.destroy();
    }

    /**
     * Apply FXAA on {@code image}.
     */
    @Override
    public Texture2D onProcess(final Texture2D image) {
        frameBuffer.bind();
        fxaaShader.bind();
        image.bind();

        renderQuad();

        image.unbind();
        Shader.unbind();
        frameBuffer.unbind();
        return frameBuffer.getColorTexture(0);
    }
}
