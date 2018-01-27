package com.adrien.games.bagl.rendering.postprocess;

import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.FrameBufferParameters;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.MeshFactory;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.DoubleBuffer;

import static org.lwjgl.opengl.GL11.glDrawArrays;

/**
 * Image post processor
 *
 * @author adrien
 */
public class PostProcessor {

    private static final String POST_PROCESS_VERTEX_SHADER_FILE = "/post/post_process.vert";

    private FrameBuffer bloomBuffer;
    private DoubleBuffer<FrameBuffer> blurBuffer;

    private final Shader bloomShader;
    private final Shader blurShader;
    private final Shader lastStageShader;

    private Mesh screenQuad;

    public PostProcessor(final int xResolution, final int yResolution) {
        final FrameBufferParameters parameters = FrameBufferParameters.builder().hasDepthStencil(false).colorOutputFormat(Format.RGB16F).build();
        this.bloomBuffer = new FrameBuffer(xResolution, yResolution, parameters);
        this.blurBuffer = new DoubleBuffer<>(() -> new FrameBuffer(xResolution, yResolution, parameters));

        this.bloomShader = new Shader().addVertexShader(POST_PROCESS_VERTEX_SHADER_FILE).addFragmentShader("/post/bloom.frag").compile();
        this.blurShader = new Shader().addVertexShader(POST_PROCESS_VERTEX_SHADER_FILE).addFragmentShader("/post/blur.frag").compile();
        this.lastStageShader = new Shader().addVertexShader(POST_PROCESS_VERTEX_SHADER_FILE).addFragmentShader("/post/post_process.frag").compile();
        this.screenQuad = MeshFactory.createScreenQuad();
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.bloomBuffer.destroy();
        this.blurBuffer.apply(FrameBuffer::destroy);
        this.bloomShader.destroy();
        this.blurShader.destroy();
        this.lastStageShader.destroy();
        this.screenQuad.destroy();
    }

    /**
     * Apply post processing to an image
     * <p>
     * Applies bloom, gamma correction et hdr tone mapping to the image
     *
     * @param image The image to apply post processing to
     */
    public void process(final Texture image) {
        this.screenQuad.getVertexArray().bind();
        this.performBloomPass(image);
        this.performGaussianBlur(this.bloomBuffer.getColorTexture(0));
        this.performFinalPass(image);
        this.screenQuad.getVertexArray().bind();
    }

    private void performBloomPass(final Texture image) {
        this.bloomBuffer.bind();
        this.bloomBuffer.clear();

        this.bloomShader.bind();
        image.bind();

        glDrawArrays(this.screenQuad.getPrimitiveType().getGlCode(), 0, this.screenQuad.getVertexCount());
        this.bloomBuffer.unbind();
    }

    private void performGaussianBlur(final Texture image) {
        this.blurShader.bind();
        boolean horizontal = true;

        for (int i = 0; i < 10; i++, horizontal = !horizontal) {
            this.blurBuffer.getWriteBuffer().bind();
            this.blurBuffer.getWriteBuffer().clear();
            this.blurShader.setUniform("horizontal", horizontal);

            if (i == 0) {
                image.bind();
            } else {
                this.blurBuffer.getReadBuffer().getColorTexture(0).bind();
            }

            glDrawArrays(this.screenQuad.getPrimitiveType().getGlCode(), 0, this.screenQuad.getVertexCount());

            this.blurBuffer.swap();
        }

        this.blurBuffer.getReadBuffer().unbind();
        Shader.unbind();
    }

    private void performFinalPass(final Texture baseImage) {
        this.lastStageShader.bind();
        this.lastStageShader.setUniform("image", 0);
        this.lastStageShader.setUniform("bloom", 1);
        baseImage.bind(0);
        this.blurBuffer.getReadBuffer().getColorTexture(0).bind(1);

        glDrawArrays(this.screenQuad.getPrimitiveType().getGlCode(), 0, this.screenQuad.getVertexCount());

        Texture.unbind(1);
        Texture.unbind(0);
        Shader.unbind();
    }
}
