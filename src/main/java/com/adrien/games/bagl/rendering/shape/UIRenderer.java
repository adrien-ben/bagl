package com.adrien.games.bagl.rendering.shape;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.vertex.*;
import com.adrien.games.bagl.utils.ResourcePath;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * UI element renderer.
 */
public class UIRenderer {

    private static final int BUFFER_SIZE = 1024;
    private static final int VERTICES_PER_SHAPE = 4;
    private static final int ELEMENT_PER_VERTEX = 6;
    private static final int INDICES_PER_SHAPE = 6;

    private boolean started;
    private int bufferedCount;

    private final FloatBuffer vertices;
    private final Shader shader;

    private final VertexArray vArray;
    private final VertexBuffer vBuffer;
    private final IndexBuffer iBuffer;

    public UIRenderer() {
        this.started = false;
        this.bufferedCount = 0;
        this.vertices = MemoryUtil.memAllocFloat(BUFFER_SIZE * VERTICES_PER_SHAPE * ELEMENT_PER_VERTEX);
        this.shader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/ui/shape.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/ui/shape.frag"))
                .build();

        this.vBuffer = new VertexBuffer(this.vertices, VertexBufferParams.builder()
                .element(new VertexElement(0, 2))
                .element(new VertexElement(1, 4))
                .build());

        this.vArray = new VertexArray();
        this.vArray.bind();
        this.vArray.attachVertexBuffer(this.vBuffer);
        this.vArray.unbind();

        try (final var stack = MemoryStack.stackPush()) {
            final var indices = stack.mallocInt(BUFFER_SIZE * INDICES_PER_SHAPE);
            for (var i = 0; i < BUFFER_SIZE; i++) {
                indices.put(i * INDICES_PER_SHAPE, i * VERTICES_PER_SHAPE);
                indices.put(i * INDICES_PER_SHAPE + 1, i * VERTICES_PER_SHAPE + 1);
                indices.put(i * INDICES_PER_SHAPE + 2, i * VERTICES_PER_SHAPE + 2);
                indices.put(i * INDICES_PER_SHAPE + 3, i * VERTICES_PER_SHAPE + 2);
                indices.put(i * INDICES_PER_SHAPE + 4, i * VERTICES_PER_SHAPE + 3);
                indices.put(i * INDICES_PER_SHAPE + 5, i * VERTICES_PER_SHAPE);
            }
            this.iBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        }
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
     * Renders the currently buffered batch of shapes
     */
    private void flush() {
        this.vBuffer.bind();
        this.vBuffer.update(this.vertices);
        this.vBuffer.unbind();

        this.shader.bind();
        this.vArray.bind();
        this.iBuffer.bind();

        GL11.glDrawElements(GL11.GL_TRIANGLES, this.bufferedCount * INDICES_PER_SHAPE, this.iBuffer.getDataType().getGlCode(), 0);

        this.iBuffer.unbind();
        this.vArray.unbind();
        Shader.unbind();

        this.bufferedCount = 0;
    }

    /**
     * Destroys the shape renderer
     */
    public void destroy() {
        this.shader.destroy();
        MemoryUtil.memFree(this.vertices);
        this.vBuffer.destroy();
        this.vArray.destroy();
        this.iBuffer.destroy();
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

        final var bufferIndex = this.bufferedCount * VERTICES_PER_SHAPE;
        this.setVertexData(bufferIndex, x, y, color);
        this.setVertexData(bufferIndex + 1, x + width, y, color);
        this.setVertexData(bufferIndex + 2, x + width, y + height, color);
        this.setVertexData(bufferIndex + 3, x, y + height, color);

        this.bufferedCount++;
        if (this.bufferedCount == BUFFER_SIZE) {
            this.flush();
        }
    }

    private void setVertexData(final int index, final float x, final float y, final Color color) {
        this.vertices.put(index * ELEMENT_PER_VERTEX, x);
        this.vertices.put(index * ELEMENT_PER_VERTEX + 1, y);
        this.vertices.put(index * ELEMENT_PER_VERTEX + 2, color.getRed());
        this.vertices.put(index * ELEMENT_PER_VERTEX + 3, color.getGreen());
        this.vertices.put(index * ELEMENT_PER_VERTEX + 4, color.getBlue());
        this.vertices.put(index * ELEMENT_PER_VERTEX + 5, color.getAlpha());
    }
}
