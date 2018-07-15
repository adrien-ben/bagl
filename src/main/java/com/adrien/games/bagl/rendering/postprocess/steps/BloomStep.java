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
 * Bloom post processing step.
 *
 * @author adrien
 */
public class BloomStep extends PostProcessorStep {

    private final Shader bloomShader;
    private final Shader blurShader;
    private final Shader lastStageShader;
    private FrameBuffer bloomBuffer;
    private DoubleBuffer<FrameBuffer> blurBuffer;
    private FrameBuffer finalBuffer;

    public BloomStep(final int xResolution, final int yResolution) {
        final var parameters = FrameBufferParameters.builder().hasDepthStencil(false).colorOutputFormat(Format.RGB16F).build();
        this.bloomBuffer = new FrameBuffer(xResolution, yResolution, parameters);
        this.blurBuffer = new DoubleBuffer<>(() -> new FrameBuffer(xResolution, yResolution, parameters));
        this.finalBuffer = new FrameBuffer(xResolution, yResolution, parameters);

        this.bloomShader = Shader.builder()
                .vertexPath(POST_PROCESS_VERTEX_SHADER_FILE)
                .fragmentPath("classpath:/shaders/post/bloom.frag")
                .build();
        this.blurShader = Shader.builder()
                .vertexPath(POST_PROCESS_VERTEX_SHADER_FILE)
                .fragmentPath("classpath:/shaders/post/blur.frag")
                .build();
        this.lastStageShader = Shader.builder()
                .vertexPath(POST_PROCESS_VERTEX_SHADER_FILE)
                .fragmentPath("classpath:/shaders/post/bloom_final.frag")
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * @see PostProcessorStep#onDestroy()
     */
    @Override
    public void onDestroy() {
        bloomBuffer.destroy();
        blurBuffer.apply(FrameBuffer::destroy);
        finalBuffer.destroy();
        bloomShader.destroy();
        blurShader.destroy();
        lastStageShader.destroy();
    }

    /**
     * Apply bloom on {@code image}.
     */
    @Override
    public Texture onProcess(final Texture image) {
        performBloomPass(image);
        performGaussianBlur(bloomBuffer.getColorTexture(0));
        performFinalPass(image);

        return finalBuffer.getColorTexture(0);
    }

    private void performBloomPass(final Texture image) {
        bloomBuffer.bind();
        bloomBuffer.clear();

        bloomShader.bind();
        image.bind();

        renderQuad();

        bloomBuffer.unbind();
    }

    private void performGaussianBlur(final Texture image) {
        blurShader.bind();
        var horizontal = true;

        for (var i = 0; i < 10; i++, horizontal = !horizontal) {
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
    }

    private void performFinalPass(final Texture baseImage) {
        finalBuffer.bind();
        finalBuffer.clear();

        lastStageShader.bind();
        lastStageShader.setUniform("image", 0);
        lastStageShader.setUniform("bloom", 1);
        baseImage.bind(0);
        blurBuffer.getReadBuffer().getColorTexture(0).bind(1);

        renderQuad();

        Texture.unbind(1);
        Texture.unbind(0);
        Shader.unbind();

        finalBuffer.unbind();
    }
}
