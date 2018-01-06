package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Camera2D;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import com.adrien.games.bagl.rendering.vertex.VertexPositionColorTexture;
import com.adrien.games.bagl.rendering.vertex.VertexPositionTexture;
import org.lwjgl.opengl.GL11;

/**
 * Class allowing to render sprites in batch.
 * <p>The purpose of this class is to limit the number of draw calls made when rendering sprites. A batch is rendered when :
 * <ul>
 * <li>The Spritebatch::end is called (generally before the end of the frame)
 * <li>The batch max size is reached.
 * <li>The current texture changes. (it means spritebatch works better if sprites are grouped
 * by texture or with texture atlases)
 *
 * @author Adrien
 */
public class Spritebatch {

    private static final int MAX_SIZE = 4096;
    private static final int VERTICES_PER_SPRITE = 4;
    private static final int INDICES_PER_SPRITE = 6;
    private static final float HALF_PIXEL_SIZE = 0.5f;

    private final Camera2D camera;
    private final Shader spriteShader;
    private Shader currentShader;
    private final int size;
    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;
    private final VertexPositionColorTexture[] vertices;
    private int drawnSprites;
    private boolean started;
    private Texture currentTexture;

    /**
     * Instantiates and initializes the spritebatch.
     * <p>Default camera and shader are created.
     *
     * @param size   The size of the spritebatch. Will be overridden if over {@value #MAX_SIZE}.
     * @param width  The width of the viewport.
     * @param height The height of the viewport.
     */
    public Spritebatch(final int size, final int width, final int height) {
        this.camera = new Camera2D(new Vector2(width / 2, height / 2), width, height);
        this.spriteShader = new Shader().addVertexShader("/sprite/sprite.vert").addFragmentShader("/sprite/sprite.frag").compile();
        this.size = size < MAX_SIZE ? size : MAX_SIZE;
        this.vertexBuffer = new VertexBuffer(VertexPositionColorTexture.DESCRIPTION, BufferUsage.DYNAMIC_DRAW, this.size * VERTICES_PER_SPRITE);
        this.vertices = this.initVertices(this.size);
        this.indexBuffer = this.initIndexBuffer(this.size);
        this.drawnSprites = 0;
        this.started = false;
    }

    /**
     * Initializes the index buffer.
     *
     * @param size The size of the spritebatch.
     * @return The initialized {@link IndexBuffer}.
     */
    private IndexBuffer initIndexBuffer(final int size) {
        final int[] indices = new int[size * INDICES_PER_SPRITE];
        for (int i = 0; i < size; i++) {
            final int offset = i * INDICES_PER_SPRITE;
            final int firstIndex = i * VERTICES_PER_SPRITE;
            indices[offset] = firstIndex;
            indices[offset + 1] = firstIndex + 1;
            indices[offset + 2] = firstIndex + 2;
            indices[offset + 3] = firstIndex + 2;
            indices[offset + 4] = firstIndex + 1;
            indices[offset + 5] = firstIndex + 3;
        }
        return new IndexBuffer(BufferUsage.STATIC_DRAW, indices);
    }

    /**
     * Initializes the vertex pool.
     *
     * @param size The size of the spritebatch.
     * @return An initialized array of {@link VertexPositionTexture}.
     */
    private VertexPositionColorTexture[] initVertices(final int size) {
        final VertexPositionColorTexture[] vertices = new VertexPositionColorTexture[size * VERTICES_PER_SPRITE];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new VertexPositionColorTexture(new Vector3(), new Color(1, 1, 1, 1), new Vector2());
        }
        return vertices;
    }

    /**
     * Releases owned resources.
     */
    public void destroy() {
        this.spriteShader.destroy();
        this.vertexBuffer.destroy();
        this.indexBuffer.destroy();
    }

    /**
     * Starts the spritebatching.
     * <p>
     * This method re-initializes the state of the spritebatch.
     * Can only be called once before Spritebatch::end is called.
     * This uses the default sprite shader.
     */
    public void start() {
        this.start(this.spriteShader);
    }

    /**
     * Starts the spritebatching.
     * <p>
     * This method re-initializes the state of the spritebatch.
     * Can only be called once before Spritebatch::end is called.
     * <p>
     * The passed in shader MUST accept a uniform called uCamera, this uniform
     * MUST be filled with a {@link com.adrien.games.bagl.core.math.Matrix4}.
     * The shader must accept 3 vertex attributes, the first channel (0) is for
     * position (two floats), the second for color (four floats) and the third
     * for texture coordinates (2 floats).
     *
     * @param shader The shader to use to render the new batch.
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
     * Checks that the spritebatch is started before it is used.
     */
    private void checkStarted() {
        if (!started) {
            throw new IllegalStateException("You must call Spritebatch::start before calling Spritebatch::end or Spritebatch::draw.");
        }
    }

    /**
     * Ends the spritebatching.
     * <p>Spritebatch::start must have been called before this one is called.
     * When called this methods renders the current batch.
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
     * Renders a sprite at a given position.
     *
     * @param texture  The texture to render.
     * @param position The position where to render the sprite.
     */
    public void draw(final Texture texture, final Vector2 position) {
        this.draw(texture, position, texture.getWidth(), texture.getHeight(), 0, 0, 1, 1, 0, Color.WHITE);
    }

    /**
     * Renders a sprite at a given position and with a given size.
     *
     * @param texture  The texture to render.
     * @param position The position where to render the sprite.
     * @param width    The width of the sprite to render.
     * @param height   The height of the sprite to render.
     */
    public void draw(final Texture texture, final Vector2 position, final float width, final float height) {
        this.draw(texture, position, width, height, 0, 0, 1, 1, 0, Color.WHITE);
    }

    /**
     * Renders a sprite at a given position, with a given size and a given rotation.
     *
     * @param texture  The texture to render.
     * @param position The position where to render the sprite.
     * @param width    The width of the sprite to render.
     * @param height   The height of the sprite to render.
     * @param rotation The rotation of the sprite.
     */
    public void draw(final Texture texture, final Vector2 position, final float width, final float height, final float rotation) {
        this.draw(texture, position, width, height, 0, 0, 1, 1, rotation, Color.WHITE);
    }

    /**
     * Renders a sprite at a given position, with a given size, a given rotation and a color.
     *
     * @param texture  The texture to render.
     * @param position The position where to render the sprite.
     * @param width    The width of the sprite to render.
     * @param height   The height of the sprite to render.
     * @param rotation The rotation of the sprite.
     * @param color    The tint of the sprite.
     */
    public void draw(final Texture texture, final Vector2 position, final float width, final float height, final float rotation,
                     final Color color) {
        this.draw(texture, position, width, height, 0, 0, 1, 1, rotation, color);
    }

    /**
     * Renders a texture region at a given position.
     *
     * @param region   The region to draw.
     * @param position The position where to draw the sprite.
     * @param width    The width of the sprite to render.
     * @param height   The height of the sprite to render.
     */
    public void draw(final TextureRegion region, final Vector2 position, final float width, final float height) {
        this.draw(region.getTexture(), position, width, height, region.getLeft(), region.getBottom(),
                region.getRight(), region.getTop(), 0f, Color.WHITE);
    }

    /**
     * Renders a sprite at a given position, with a given size, a given rotation and a color.
     *
     * @param texture  The texture to render.
     * @param position The position where to render the sprite.
     * @param width    The width of the sprite to render.
     * @param height   The height of the sprite to render.
     * @param rotation The rotation of the sprite.
     * @param color    The tint of the sprite.
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
     * Compute the final position of a vertex.
     *
     * @param index    The index of the vertex.
     * @param x        The initial x position of the vertex.
     * @param y        The initial y position of the vertex.
     * @param xCoord   The x texture coordinate.
     * @param yCoord   The y texture coordinate.
     * @param rotation The rotation of the vertex.
     * @param xCenter  The x component of the rotation center.
     * @param yCenter  The y component of the rotation center.
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

        this.vertices[index].getPosition().setX(finalX);
        this.vertices[index].getPosition().setY(finalY);
        this.vertices[index].getColor().setRed(color.getRed());
        this.vertices[index].getColor().setGreen(color.getGreen());
        this.vertices[index].getColor().setBlue(color.getBlue());
        this.vertices[index].getCoords().setX(xCoord);
        this.vertices[index].getCoords().setY(yCoord);
    }

    /**
     * Renders the current batch.
     * <ul>
     * <li>Binds the shader and texture.
     * <li>Updates the vertex buffer data.
     * <li>Binds the vertex and index buffers.
     * <li>Performs the draw call.
     * <li>Unbinds everything.
     */
    private void renderBatch() {
        if (this.drawnSprites > 0) {
            this.currentShader.bind();
            this.currentShader.setUniform("uCamera", this.camera.getOrthographic());
            this.currentTexture.bind();

            this.vertexBuffer.setData(this.vertices, this.drawnSprites * VERTICES_PER_SPRITE);
            this.vertexBuffer.bind();
            this.indexBuffer.bind();

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDrawElements(GL11.GL_TRIANGLES, this.drawnSprites * INDICES_PER_SPRITE, GL11.GL_UNSIGNED_INT, 0);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            this.drawnSprites = 0;

            IndexBuffer.unbind();
            VertexBuffer.unbind();
            Texture.unbind();
            Shader.unbind();
        }
    }

}
