package com.adrienben.games.bagl.engine.rendering.sprite;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.camera.Camera2D;
import com.adrienben.games.bagl.engine.rendering.renderer.Renderer;
import com.adrienben.games.bagl.opengl.BufferUsage;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.games.bagl.opengl.vertex.*;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * Class allowing to render sprites in batch
 * <p>
 * The purpose of this class is to limit the number of draw calls made when rendering sprites. A batch is rendered when :
 * <ul>
 * <li>The Spritebatch::end is called (generally before the end of the frame)
 * <li>The batch max size is reached
 * <li>The current texture changes (it means spritebatch works better if sprites are grouped
 * by texture or with texture atlases)
 *
 * @author Adrien
 */
public class Spritebatch implements Renderer<Sprite> {

    private static final int MAX_SIZE = 4096;
    private static final float HALF_PIXEL_SIZE = 0.5f;

    private static final int INDICES_PER_SPRITE = 6;
    private static final int VERTICES_PER_SPRITE = 4;
    private static final int ELEMENTS_PER_VERTICES = 8;
    private static final int POSITION_INDEX = 0;
    private static final int ELEMENTS_PER_POSITION = 3;
    private static final int COLOR_INDEX = 1;
    private static final int ELEMENTS_PER_COLOR = 3;
    private static final int COORDINATES_INDEX = 2;
    private static final int ELEMENTS_PER_COORDINATES = 2;

    private final Camera2D camera;
    private final Shader spriteShader;
    private Shader currentShader;
    private final int size;

    private final FloatBuffer vertices;
    private VertexBuffer vBuffer;
    private VertexArray vArray;
    private IndexBuffer iBuffer;

    private int drawnSprites;
    private boolean started;
    private Texture2D currentTexture;

    /**
     * Instantiates and initializes the spritebatch
     * <p>
     * Default camera and shader are created
     *
     * @param size   The size of the spritebatch. Will be overridden if over {@value #MAX_SIZE}
     * @param width  The width of the viewport
     * @param height The height of the viewport
     */
    public Spritebatch(final int size, final int width, final int height) {
        if (size > MAX_SIZE) {
            throw new EngineException("Maximum size for Spritebatch is " + MAX_SIZE);
        }
        this.size = size;

        this.camera = new Camera2D(new Vector2f(width / 2, height / 2), width, height);
        this.spriteShader = Shader.pipelineBuilder()
                .vertexPath(ResourcePath.get("classpath:/shaders/sprite/sprite.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/sprite/sprite.frag"))
                .build();

        this.vertices = MemoryUtil.memAllocFloat(this.size * VERTICES_PER_SPRITE * ELEMENTS_PER_VERTICES);
        initVertices();

        this.iBuffer = initIndices();

        this.drawnSprites = 0;
        this.started = false;
    }

    /**
     * Initialize the vertex array and buffer
     */
    private void initVertices() {
        vBuffer = new VertexBuffer(vertices, VertexBufferParams.builder()
                .usage(BufferUsage.DYNAMIC_DRAW)
                .element(new VertexElement(POSITION_INDEX, ELEMENTS_PER_POSITION))
                .element(new VertexElement(COLOR_INDEX, ELEMENTS_PER_COLOR))
                .element(new VertexElement(COORDINATES_INDEX, ELEMENTS_PER_COORDINATES))
                .build());
        vArray = new VertexArray();
        vArray.bind();
        vArray.attachVertexBuffer(vBuffer);
        vArray.unbind();
    }

    /**
     * Initialize the index buffer
     */
    private IndexBuffer initIndices() {
        try (final var stack = MemoryStack.stackPush()) {
            final var indices = stack.mallocShort(size * INDICES_PER_SPRITE);
            for (var i = 0; i < size; i++) {
                final var offset = i * INDICES_PER_SPRITE;
                final var firstIndex = i * VERTICES_PER_SPRITE;
                indices.put(offset, (short) firstIndex);
                indices.put(offset + 1, (short) (firstIndex + 1));
                indices.put(offset + 2, (short) (firstIndex + 2));
                indices.put(offset + 3, (short) (firstIndex + 2));
                indices.put(offset + 4, (short) (firstIndex + 1));
                indices.put(offset + 5, (short) (firstIndex + 3));
            }
            return new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        }
    }

    /**
     * Release owned resources
     */
    public void destroy() {
        spriteShader.destroy();
        MemoryUtil.memFree(vertices);
        iBuffer.destroy();
        vBuffer.destroy();
        vArray.destroy();
    }

    /**
     * Start the spritebatching
     * <p>
     * This method re-initializes the state of the spritebatch.
     * It can only be called once before Spritebatch::end is called.
     * This uses the default sprite shader
     */
    public void start() {
        start(spriteShader);
    }

    /**
     * Start the spritebatching
     * <p>
     * This method re-initializes the state of the spritebatch.
     * Can only be called once before Spritebatch::end is called
     * <p>
     * The passed in shader MUST accept a uniform called uCamera, this uniform
     * MUST be filled with a {@link org.joml.Matrix4f}.
     * The shader must accept 3 vertex attributes, the first channel (0) is for
     * position (two floats), the second for color (four floats) and the third
     * for texture coordinates (2 floats)
     *
     * @param shader The shader to use to render the new batch
     */
    public void start(final Shader shader) {
        if (started) {
            throw new IllegalStateException("You must call Spritebatch::end before calling Spritebatch::start again.");
        }
        currentShader = shader;
        drawnSprites = 0;
        started = true;
    }

    /**
     * Check that the spritebatch is started before it is used
     */
    private void checkStarted() {
        if (!started) {
            throw new IllegalStateException("You must call Spritebatch::start before calling Spritebatch::end or Spritebatch::render.");
        }
    }

    /**
     * End the spritebatching
     * <p>
     * Spritebatch::start must have been called before this one is called.
     * When called this methods renders the current batch
     */
    public void end() {
        checkStarted();
        renderBatch();
        started = false;
    }

    private boolean shouldRender(final Texture2D texture) {
        return drawnSprites >= size || (currentTexture != null && !currentTexture.equals(texture));
    }

    /**
     * Render {@code sprite}.
     * <p>
     * {@link Spritebatch#start()} must have been called before and {@link Spritebatch#end()} must not.
     */
    @Override
    public void render(final Sprite sprite) {
        final Texture2D texture = sprite.getTexture();

        checkStarted();
        if (shouldRender(texture)) {
            renderBatch();
        }

        currentTexture = texture;
        computeVertices(sprite);
        drawnSprites++;
    }

    private void computeVertices(final Sprite sprite) {
        final Vector2fc position = sprite.getPosition();
        final float width = sprite.getWidth();
        final float height = sprite.getHeight();
        final float texRegionLeft = sprite.getRegion().minX;
        final float texRegionBottom = sprite.getRegion().minY;
        final float texRegionRight = sprite.getRegion().maxX;
        final float texRegionTop = sprite.getRegion().maxY;
        final float rotation = sprite.getRotation();
        final Color color = sprite.getColor();

        final var halfPixelWidth = HALF_PIXEL_SIZE / currentTexture.getWidth();
        final var halfPixelHeight = HALF_PIXEL_SIZE / currentTexture.getHeight();

        final var x = position.x();
        final var y = position.y();

        final var xCenter = x + width / 2;
        final var yCenter = y + height / 2;

        final var offset = drawnSprites * VERTICES_PER_SPRITE;

        computeVertex(offset, x, y, texRegionLeft + halfPixelWidth, texRegionBottom + halfPixelHeight,
                rotation, xCenter, yCenter, color);
        computeVertex(offset + 1, x + width, y, texRegionRight - halfPixelWidth, texRegionBottom + halfPixelHeight,
                rotation, xCenter, yCenter, color);
        computeVertex(offset + 2, x, y + height, texRegionLeft + halfPixelWidth, texRegionTop - halfPixelHeight,
                rotation, xCenter, yCenter, color);
        computeVertex(offset + 3, x + width, y + height, texRegionRight - halfPixelWidth, texRegionTop - halfPixelHeight,
                rotation, xCenter, yCenter, color);
    }

    /**
     * Compute the final position of a vertex
     *
     * @param index    The index of the vertex
     * @param x        The initial x position of the vertex
     * @param y        The initial y position of the vertex
     * @param xCoord   The x texture coordinate
     * @param yCoord   The y texture coordinate
     * @param rotation The rotation of the vertex
     * @param xCenter  The x component of the rotation center
     * @param yCenter  The y component of the rotation center
     */
    private void computeVertex(final int index, final float x, final float y, final float xCoord, final float yCoord,
                               final float rotation, final float xCenter, final float yCenter, final Color color) {
        var finalX = x;
        var finalY = y;

        if (rotation != 0) {
            final var xOrigin = x - xCenter;
            final var yOrigin = y - yCenter;

            final var angleInRads = -(rotation * Math.PI / 180f);
            final var cos = Math.cos(angleInRads);
            final var sin = Math.sin(angleInRads);

            final var _x = xOrigin * cos - yOrigin * sin;
            final var _y = xOrigin * sin + yOrigin * cos;

            finalX = (float) _x + xCenter;
            finalY = (float) _y + yCenter;
        }

        vertices.put(index * ELEMENTS_PER_VERTICES, finalX);
        vertices.put(index * ELEMENTS_PER_VERTICES + 1, finalY);
        vertices.put(index * ELEMENTS_PER_VERTICES + 2, 0);
        vertices.put(index * ELEMENTS_PER_VERTICES + 3, color.getRed());
        vertices.put(index * ELEMENTS_PER_VERTICES + 4, color.getGreen());
        vertices.put(index * ELEMENTS_PER_VERTICES + 5, color.getBlue());
        vertices.put(index * ELEMENTS_PER_VERTICES + 6, xCoord);
        vertices.put(index * ELEMENTS_PER_VERTICES + 7, yCoord);
    }

    /**
     * Render the current batch :
     * <ul>
     * <li>Binds the shader and texture
     * <li>Updates the vertex buffer data
     * <li>Binds the vertex and index buffers
     * <li>Performs the draw call
     * <li>Unbinds everything
     */
    private void renderBatch() {
        if (drawnSprites > 0) {
            updateVertices();
            bindGLResources();
            performDrawCall();
            drawnSprites = 0;
            unbindGLResources();
        }
    }

    private void updateVertices() {
        vBuffer.bind();
        vBuffer.update(vertices);
        vBuffer.unbind();
    }

    private void bindGLResources() {
        currentShader.bind();
        currentShader.setUniform("uCamera", camera.getOrthographic());
        currentTexture.bind();
        vArray.bind();
        iBuffer.bind();
    }

    private void performDrawCall() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDrawElements(GL11.GL_TRIANGLES, drawnSprites * INDICES_PER_SPRITE, iBuffer.getDataType().getGlCode(), 0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void unbindGLResources() {
        iBuffer.unbind();
        vArray.unbind();
        currentTexture.unbind();
        Shader.unbind();
    }
}
