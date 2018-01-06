package com.adrien.games.bagl.rendering.postprocess;

import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPositionTexture;
import com.adrien.games.bagl.utils.DoubleBuffer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
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

    private final VertexBuffer vertexBuffer;

    public PostProcessor(final int xResolution, final int yResolution) {
        this.bloomBuffer = new FrameBuffer(xResolution, yResolution, new FrameBufferParameters().addColorOutput(Format.RGBA32F));
        this.blurBuffer = new DoubleBuffer<>(
                () -> new FrameBuffer(xResolution, yResolution, new FrameBufferParameters().addColorOutput(Format.RGBA32F)));

        this.bloomShader = new Shader().addVertexShader(POST_PROCESS_VERTEX_SHADER_FILE).addFragmentShader("/post/bloom.frag").compile();
        this.blurShader = new Shader().addVertexShader(POST_PROCESS_VERTEX_SHADER_FILE).addFragmentShader("/post/blur.frag").compile();
        this.lastStageShader = new Shader().addVertexShader(POST_PROCESS_VERTEX_SHADER_FILE).addFragmentShader("/post/post_process.frag").compile();
        this.vertexBuffer = this.initQuad();
    }

    private VertexBuffer initQuad() {
        final Vertex[] vertices = new Vertex[4];
        vertices[0] = new VertexPositionTexture(new Vector3(-1, -1, 0), new Vector2(0, 0));
        vertices[1] = new VertexPositionTexture(new Vector3(1, -1, 0), new Vector2(1, 0));
        vertices[2] = new VertexPositionTexture(new Vector3(-1, 1, 0), new Vector2(0, 1));
        vertices[3] = new VertexPositionTexture(new Vector3(1, 1, 0), new Vector2(1, 1));
        return new VertexBuffer(VertexPositionTexture.DESCRIPTION, BufferUsage.STATIC_DRAW, vertices);
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
        this.vertexBuffer.destroy();
    }

    /**
     * Apply post processing to an image
     * <p>
     * Applies bloom, gamma correction et hdr tone mapping to the image
     *
     * @param image The image to apply post processing to
     */
    public void process(final Texture image) {
        this.vertexBuffer.bind();
        this.performBloomPass(image);
        this.performGaussianBlur(this.bloomBuffer.getColorTexture(0));
        this.performFinalPass(image);
        VertexBuffer.unbind();
    }

    private void performBloomPass(final Texture image) {
        this.bloomBuffer.bind();
        this.bloomShader.bind();
        image.bind();

        FrameBuffer.clear();
        glDrawArrays(GL_TRIANGLE_STRIP, 0, this.vertexBuffer.getVertexCount());
        FrameBuffer.unbind();
    }

    private void performGaussianBlur(final Texture image) {
        this.blurShader.bind();
        boolean horizontal = true;

        for (int i = 0; i < 10; i++, horizontal = !horizontal) {
            this.blurBuffer.getWriteBuffer().bind();
            FrameBuffer.clear();
            this.blurShader.setUniform("horizontal", horizontal);

            if (i == 0) {
                image.bind();
            } else {
                this.blurBuffer.getReadBuffer().getColorTexture(0).bind();
            }

            glDrawArrays(GL_TRIANGLE_STRIP, 0, this.vertexBuffer.getVertexCount());

            this.blurBuffer.swap();
        }

        FrameBuffer.unbind();
        Shader.unbind();
    }

    private void performFinalPass(final Texture baseImage) {
        this.lastStageShader.bind();
        this.lastStageShader.setUniform("image", 0);
        this.lastStageShader.setUniform("bloom", 1);
        baseImage.bind(0);
        this.blurBuffer.getReadBuffer().getColorTexture(0).bind(1);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, this.vertexBuffer.getVertexCount());

        Texture.unbind(1);
        Texture.unbind(0);
        Shader.unbind();
    }
}
