package com.adrien.games.bagl.rendering.shape;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.Shader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * UI element renderer.
 */
public class UIRenderer {

    private static final int BUFFER_SIZE = 1024;
    private static final int VERTICES_PER_SHAPE = 4;
    private static final int ELEMENT_PER_VERTEX = 6;
    private static final int INDICES_PER_SHAPE = 6;
    private static final int VERTEX_STRIDE = ELEMENT_PER_VERTEX * Float.SIZE / 8;

    private boolean started;
    private int bufferedCount;

    private final FloatBuffer vertexBuffer;
    private final IntBuffer indexBuffer;
    private final Shader shader;

    private final int vao;
    private final int vbo;
    private final int ibo;

    public UIRenderer() {
        this.started = false;
        this.bufferedCount = 0;
        this.vertexBuffer = MemoryUtil.memAllocFloat(BUFFER_SIZE * VERTICES_PER_SHAPE * ELEMENT_PER_VERTEX);
        this.indexBuffer = MemoryUtil.memAllocInt(BUFFER_SIZE * INDICES_PER_SHAPE);
        this.shader = new Shader().addVertexShader("shape.vert").addFragmentShader("shape.frag").compile();

        this.vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(this.vao);

        this.vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vertexBuffer, GL15.GL_DYNAMIC_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, VERTEX_STRIDE, 0);

        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, VERTEX_STRIDE, 2 * Float.SIZE / 8);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        for (int i = 0; i < BUFFER_SIZE; i++) {
            this.indexBuffer.put(i * VERTICES_PER_SHAPE);
            this.indexBuffer.put(i * VERTICES_PER_SHAPE + 1);
            this.indexBuffer.put(i * VERTICES_PER_SHAPE + 2);
            this.indexBuffer.put(i * VERTICES_PER_SHAPE + 2);
            this.indexBuffer.put(i * VERTICES_PER_SHAPE + 3);
            this.indexBuffer.put(i * VERTICES_PER_SHAPE);
        }
        this.indexBuffer.flip();

        this.ibo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Starts the buffering of frames to render.
     * <p>
     * This method must be call before starting to render any shapes.
     */
    public void start() {
        if (this.started) {
            throw new IllegalStateException("ShaderRenderer#start has already been called. You must call ShaderRenderer#end " +
                    "before calling it again.");
        }
        this.started = true;
    }

    /**
     * Renders the current batch of shapes.
     * <p>
     * This method must be called after {@link UIRenderer#start()}.
     */
    public void end() {
        if (!this.started) {
            throw new IllegalStateException("ShaderRenderer#end has already been called before ShaderRenderer#start.");
        }
        this.started = false;
        this.flush();
    }

    /**
     * Renders the currently buffered batch of shapes.
     */
    private void flush() {
        this.vertexBuffer.flip();
        this.indexBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, this.vertexBuffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        this.shader.bind();
        GL30.glBindVertexArray(this.vao);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo);

        GL11.glDrawElements(GL11.GL_TRIANGLES, this.bufferedCount * INDICES_PER_SHAPE, GL11.GL_UNSIGNED_INT, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        Shader.unbind();

        this.vertexBuffer.clear();
        this.indexBuffer.clear();
        this.bufferedCount = 0;
    }

    /**
     * Destroys the shape renderer.
     */
    public void destroy() {
        this.shader.destroy();
        MemoryUtil.memFree(this.vertexBuffer);
        MemoryUtil.memFree(this.indexBuffer);
        GL30.glDeleteVertexArrays(this.vao);
        GL15.glDeleteBuffers(this.vbo);
        GL15.glDeleteBuffers(this.ibo);
    }

    /**
     * Renders a colored box.
     * <p>
     * Coordinates and dimensions must be expressed in screen-space ([0; 1] on each
     * axis (0, 0) being the bottom-left corner and (1, 1) the top-right corner).
     *
     * @param x      The x position of the bottom-left corner.
     * @param y      The y position of the bottom-left corner.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     * @param color  The color of the rectangle.
     */
    public void renderBox(final float x, final float y, final float width, final float height, final Color color) {
        if (!this.started) {
            throw new IllegalStateException("ShaderRenderer#renderBox has already been called before ShaderRenderer#start.");
        }

        this.vertexBuffer.put(x);
        this.vertexBuffer.put(y);
        this.vertexBuffer.put(color.getRed());
        this.vertexBuffer.put(color.getGreen());
        this.vertexBuffer.put(color.getBlue());
        this.vertexBuffer.put(color.getAlpha());

        this.vertexBuffer.put(x + width);
        this.vertexBuffer.put(y);
        this.vertexBuffer.put(color.getRed());
        this.vertexBuffer.put(color.getGreen());
        this.vertexBuffer.put(color.getBlue());
        this.vertexBuffer.put(color.getAlpha());

        this.vertexBuffer.put(x + width);
        this.vertexBuffer.put(y + height);
        this.vertexBuffer.put(color.getRed());
        this.vertexBuffer.put(color.getGreen());
        this.vertexBuffer.put(color.getBlue());
        this.vertexBuffer.put(color.getAlpha());

        this.vertexBuffer.put(x);
        this.vertexBuffer.put(y + height);
        this.vertexBuffer.put(color.getRed());
        this.vertexBuffer.put(color.getGreen());
        this.vertexBuffer.put(color.getBlue());
        this.vertexBuffer.put(color.getAlpha());

        this.bufferedCount++;
        if (this.bufferedCount == BUFFER_SIZE) {
            this.flush();
        }
    }

}
