package com.adrienben.games.bagl.engine.rendering.postprocess.steps;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.utils.DoubleBuffer;
import com.adrienben.games.bagl.engine.rendering.postprocess.PostProcessorStep;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.Texture2D;

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

        this.bloomShader = buildProcessShader(ResourcePath.get("classpath:/shaders/post/bloom.frag"));
        this.blurShader = buildProcessShader(ResourcePath.get("classpath:/shaders/post/blur.frag"));
        this.lastStageShader = buildProcessShader(ResourcePath.get("classpath:/shaders/post/bloom_final.frag"));
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
    public Texture2D onProcess(final Texture2D image) {
        performBloomPass(image);
        performGaussianBlur(bloomBuffer.getColorTexture(0));
        performFinalPass(image);

        return finalBuffer.getColorTexture(0);
    }

    private void performBloomPass(final Texture2D image) {
        bloomBuffer.bind();
        bloomBuffer.clear();
        bloomShader.bind();
        image.bind();

        renderQuad();

        image.unbind();
        bloomBuffer.unbind();
    }

    private void performGaussianBlur(final Texture2D image) {
        blurShader.bind();
        var horizontal = true;

        for (var i = 0; i < 10; i++, horizontal = !horizontal) {
            blurBuffer.getWriteBuffer().bind();
            blurBuffer.getWriteBuffer().clear();
            blurShader.setUniform("horizontal", horizontal);

            final var texture = i == 0 ? image : blurBuffer.getReadBuffer().getColorTexture(0);
            texture.bind();

            renderQuad();

            texture.unbind();
            blurBuffer.swap();
        }

        blurBuffer.getReadBuffer().unbind();
        Shader.unbind();
    }

    private void performFinalPass(final Texture2D baseImage) {
        finalBuffer.bind();
        finalBuffer.clear();

        lastStageShader.bind();
        lastStageShader.setUniform("image", 0);
        lastStageShader.setUniform("bloom", 1);
        baseImage.bind(0);
        blurBuffer.getReadBuffer().getColorTexture(0).bind(1);

        renderQuad();

        blurBuffer.getReadBuffer().getColorTexture(0).unbind();
        baseImage.unbind();
        Shader.unbind();

        finalBuffer.unbind();
    }
}
