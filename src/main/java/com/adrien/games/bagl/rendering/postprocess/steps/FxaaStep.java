package com.adrien.games.bagl.rendering.postprocess.steps;

import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.FrameBufferParameters;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.postprocess.PostProcessorStep;
import com.adrien.games.bagl.rendering.postprocess.fxaa.FxaaPresets;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.ResourcePath;
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
    public Texture onProcess(final Texture image) {
        frameBuffer.bind();
        fxaaShader.bind();
        image.bind();

        renderQuad();

        Texture.unbind();
        Shader.unbind();
        frameBuffer.unbind();
        return frameBuffer.getColorTexture(0);
    }
}
