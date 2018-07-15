package com.adrien.games.bagl.rendering.postprocess.steps;

import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.FrameBufferParameters;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.postprocess.PostProcessorStep;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.DoubleBuffer;

import static com.adrien.games.bagl.rendering.postprocess.PostProcessor.POST_PROCESS_VERTEX_SHADER_FILE;

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
        this.blurShader = Shader.builder()
                .vertexPath(POST_PROCESS_VERTEX_SHADER_FILE)
                .fragmentPath("classpath:/shaders/post/blur.frag")
                .build();
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
