package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Camera2D;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import com.adrien.games.bagl.rendering.vertex.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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
public class Spritebatch {

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
    private Texture currentTexture;

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

        this.camera = new Camera2D(new Vector2(width / 2, height / 2), width, height);
        this.spriteShader = new Shader()
                .addVertexShader("/sprite/sprite.vert")
                .addFragmentShader("/sprite/sprite.frag")
                .compile();

        this.vertices = MemoryUtil.memAllocFloat(this.size * VERTICES_PER_SPRITE * ELEMENTS_PER_VERTICES);
        this.initVertices();

        this.iBuffer = this.initIndices();

        this.drawnSprites = 0;
        this.started = false;
    }

    /**
     * Initialize the vertex array and buffer
     */
    private void initVertices() {
        this.vBuffer = new VertexBuffer(this.vertices, new VertexBufferParams()
                .usage(BufferUsage.DYNAMIC_DRAW)
                .element(new VertexElement(POSITION_INDEX, ELEMENTS_PER_POSITION))
                .element(new VertexElement(COLOR_INDEX, ELEMENTS_PER_COLOR))
                .element(new VertexElement(COORDINATES_INDEX, ELEMENTS_PER_COORDINATES)));
        this.vArray = new VertexArray();
        this.vArray.bind();
        this.vArray.attachVertexBuffer(this.vBuffer);
        this.vArray.unbind();
    }

    /**
     * Initialize the index buffer
     */
    private IndexBuffer initIndices() {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ShortBuffer indices = stack.mallocShort(this.size * INDICES_PER_SPRITE);
            for (int i = 0; i < this.size; i++) {
                final int offset = i * INDICES_PER_SPRITE;
                final int firstIndex = i * VERTICES_PER_SPRITE;
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
        this.spriteShader.destroy();
        MemoryUtil.memFree(this.vertices);
        this.iBuffer.destroy();
        this.vBuffer.destroy();
        this.vArray.destroy();
    }

    /**
     * Start the spritebatching
     * <p>
     * This method re-initializes the state of the spritebatch.
     * It can only be called once before Spritebatch::end is called.
     * This uses the default sprite shader
     */
    public void start() {
        this.start(this.spriteShader);
    }

    /**
     * Start the spritebatching
     * <p>
     * This method re-initializes the state of the spritebatch.
     * Can only be called once before Spritebatch::end is called
     * <p>
     * The passed in shader MUST accept a uniform called uCamera, this uniform
     * MUST be filled with a {@link com.adrien.games.bagl.core.math.Matrix4}.
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
        this.currentShader = shader;
        this.drawnSprites = 0;
        this.started = true;
    }

    /**
     * Check that the spritebatch is started before it is used
     */
    private void checkStarted() {
        if (!started) {
            throw new IllegalStateException("You must call Spritebatch::start before calling Spritebatch::end or Spritebatch::draw.");
        }
    }

    /**
     * End the spritebatching
     * <p>
     * Spritebatch::start must have been called before this one is called.
     * When called this methods renders the current batch
     */
    public void end() {
        this.checkStarted();
        this.renderBatch();
        this.started = false;
    }

    private boolean shouldRender(final Texture texture) {
        return this.drawnSprites >= this.size || (this.currentTexture != null && texture != this.currentTexture);
    }

    /**
     * Render a sprite at a given position
     *
     * @param texture  The texture to render
     * @param position The position where to render the sprite
     */
    public void draw(final Texture texture, final Vector2 position) {
        this.draw(texture, position, texture.getWidth(), texture.getHeight(), 0, 0, 1, 1, 0, Color.WHITE);
    }

    /**
     * Render a sprite at a given position and with a given size
     *
     * @param texture  The texture to render
     * @param position The position where to render the sprite
     * @param width    The width of the sprite to render
     * @param height   The height of the sprite to render
     */
    public void draw(final Texture texture, final Vector2 position, final float width, final float height) {
        this.draw(texture, position, width, height, 0, 0, 1, 1, 0, Color.WHITE);
    }

    /**
     * Render a sprite at a given position, with a given size and a given rotation
     *
     * @param texture  The texture to render
     * @param position The position where to render the sprite
     * @param width    The width of the sprite to render
     * @param height   The height of the sprite to render
     * @param rotation The rotation of the sprite
     */
    public void draw(final Texture texture, final Vector2 position, final float width, final float height, final float rotation) {
        this.draw(texture, position, width, height, 0, 0, 1, 1, rotation, Color.WHITE);
    }

    /**
     * Render a sprite at a given position, with a given size, a given rotation and a color
     *
     * @param texture  The texture to render
     * @param position The position where to render the sprite
     * @param width    The width of the sprite to render
     * @param height   The height of the sprite to render
     * @param rotation The rotation of the sprite
     * @param color    The tint of the sprite
     */
    public void draw(final Texture texture, final Vector2 position, final float width, final float height, final float rotation,
                     final Color color) {
        this.draw(texture, position, width, height, 0, 0, 1, 1, rotation, color);
    }

    /**
     * Render a texture region at a given position
     *
     * @param region   The region to draw
     * @param position The position where to draw the sprite
     * @param width    The width of the sprite to render
     * @param height   The height of the sprite to render
     */
    public void draw(final TextureRegion region, final Vector2 position, final float width, final float height) {
        this.draw(region.getTexture(), position, width, height, region.getLeft(), region.getBottom(),
                region.getRight(), region.getTop(), 0f, Color.WHITE);
    }

    /**
     * Render a sprite at a given position, with a given size, a given rotation and a color
     *
     * @param texture  The texture to render
     * @param position The position where to render the sprite
     * @param width    The width of the sprite to render
     * @param height   The height of the sprite to render
     * @param rotation The rotation of the sprite
     * @param color    The tint of the sprite
     */
    public void draw(final Texture texture, final Vector2 position, final float width, final float height,
                     final float texRegionLeft, final float texRegionBottom, final float texRegionRight,
                     final float texRegionTop, final float rotation, final Color color) {
        this.checkStarted();

        if (shouldRender(texture)) {
            renderBatch();
        }

        this.currentTexture = texture;

        final float halfPixelWidth = HALF_PIXEL_SIZE / texture.getWidth();
        final float halfPixelHeight = HALF_PIXEL_SIZE / texture.getHeight();

        final float x = position.getX();
        final float y = position.getY();

        final float xCenter = x + width / 2;
        final float yCenter = y + height / 2;

        final int offset = this.drawnSprites * VERTICES_PER_SPRITE;

        this.computeVertex(offset, x, y, texRegionLeft + halfPixelWidth, texRegionBottom + halfPixelHeight,
                rotation, xCenter, yCenter, color);
        this.computeVertex(offset + 1, x + width, y, texRegionRight - halfPixelWidth, texRegionBottom + halfPixelHeight,
                rotation, xCenter, yCenter, color);
        this.computeVertex(offset + 2, x, y + height, texRegionLeft + halfPixelWidth, texRegionTop - halfPixelHeight,
                rotation, xCenter, yCenter, color);
        this.computeVertex(offset + 3, x + width, y + height, texRegionRight - halfPixelWidth, texRegionTop - halfPixelHeight,
                rotation, xCenter, yCenter, color);

        this.drawnSprites++;
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
        float finalX = x;
        float finalY = y;

        if (rotation != 0) {
            final float xOrigin = x - xCenter;
            final float yOrigin = y - yCenter;

            final double angleInRads = -(rotation * Math.PI / 180f);
            final double cos = Math.cos(angleInRads);
            final double sin = Math.sin(angleInRads);

            final double _x = xOrigin * cos - yOrigin * sin;
            final double _y = xOrigin * sin + yOrigin * cos;

            finalX = (float) _x + xCenter;
            finalY = (float) _y + yCenter;
        }

        this.vertices.put(index * ELEMENTS_PER_VERTICES, finalX);
        this.vertices.put(index * ELEMENTS_PER_VERTICES + 1, finalY);
        this.vertices.put(index * ELEMENTS_PER_VERTICES + 2, 0);

        this.vertices.put(index * ELEMENTS_PER_VERTICES + 3, color.getRed());
        this.vertices.put(index * ELEMENTS_PER_VERTICES + 4, color.getGreen());
        this.vertices.put(index * ELEMENTS_PER_VERTICES + 5, color.getBlue());

        this.vertices.put(index * ELEMENTS_PER_VERTICES + 6, xCoord);
        this.vertices.put(index * ELEMENTS_PER_VERTICES + 7, yCoord);
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
        if (this.drawnSprites > 0) {
            this.currentShader.bind();
            this.currentShader.setUniform("uCamera", this.camera.getOrthographic());
            this.currentTexture.bind();

            this.vBuffer.bind();
            this.vBuffer.update(this.vertices);
            this.vBuffer.unbind();

            this.vArray.bind();
            this.iBuffer.bind();

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDrawElements(GL11.GL_TRIANGLES, this.drawnSprites * INDICES_PER_SPRITE, this.iBuffer.getDataType().getGlCode(), 0);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            this.drawnSprites = 0;

            this.iBuffer.unbind();
            this.vArray.unbind();
            Texture.unbind();
            Shader.unbind();
        }
    }
}
