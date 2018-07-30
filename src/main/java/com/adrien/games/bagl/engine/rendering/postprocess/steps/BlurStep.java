package com.adrien.games.bagl.engine.rendering.postprocess.steps;

import com.adrien.games.bagl.core.io.ResourcePath;
import com.adrien.games.bagl.core.utils.DoubleBuffer;
import com.adrien.games.bagl.engine.rendering.postprocess.PostProcessorStep;
import com.adrien.games.bagl.opengl.FrameBuffer;
import com.adrien.games.bagl.opengl.FrameBufferParameters;
import com.adrien.games.bagl.opengl.shader.Shader;
import com.adrien.games.bagl.opengl.texture.Format;
import com.adrien.games.bagl.opengl.texture.Texture;

/**
 * Gaussian blur post processing step.
 *
 * @author adrien
 */
public class BlurStep extends PostProcessorStep {

    private static final int BLUR_PASS_COUNT = 10;

    private final DoubleBuffer<FrameBuffer> blurBuffer;
    private final Shader blurShader;

    public BlurStep(final int xResolution, final int yResolution) {
        final var parameters = FrameBufferParameters.builder().hasDepthStencil(false).colorOutputFormat(Format.RGB16F).build();
        this.blurBuffer = new DoubleBuffer<>(() -> new FrameBuffer(xResolution, yResolution, parameters));
        this.blurShader = buildProcessShader(ResourcePath.get("classpath:/shaders/post/blur.frag"));
    }

    @Override
    protected void onDestroy() {
        blurBuffer.apply(FrameBuffer::destroy);
        blurShader.destroy();
    }

    /**
     * Apply a gaussian blur filter on {@code image}.
     */
    @Override
    protected Texture onProcess(final Texture image) {
        blurShader.bind();
        var horizontal = true;

        for (var i = 0; i < BLUR_PASS_COUNT; i++, horizontal = !horizontal) {
            blurBuffer.getWriteBuffer().bind();
            blurBuffer.getWriteBuffer().clear();
            blurShader.setUniform("horizontal", horizontal);

            if (i == 0) {
                image.bind();
            } else {
                blurBuffer.getReadBuffer().getColorTexture(0).bind();
            }

            renderQuad();

            blurBuffer.swap();
        }

        blurBuffer.getReadBuffer().unbind();
        Shader.unbind();
        return blurBuffer.getReadBuffer().getColorTexture(0);
    }
}
