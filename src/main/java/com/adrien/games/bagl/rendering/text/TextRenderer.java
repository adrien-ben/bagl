package com.adrien.games.bagl.rendering.text;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import com.adrien.games.bagl.rendering.vertex.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;

/**
 * Text renderer
 *
 * @author adrien
 */
public class TextRenderer {

    private static final int MAX_TEXT_LENGTH = 1000;
    private static final float HALF_SCREEN_SIZE = 1f;

    private static final int VERTICES_PER_CHAR = 4;
    private static final int INDICES_PER_CHAR = 6;
    private static final int ELEMENTS_PER_VERTEX = 8;
    private static final int POSITION_INDEX = 0;
    private static final int ELEMENTS_PER_POSITION = 2;
    private static final int COORDINATES_INDEX = 1;
    private static final int ELEMENTS_PER_COORDINATES = 2;
    private static final int COLOR_INDEX = 2;
    private static final int ELEMENTS_PER_COLOR = 4;

    private final FloatBuffer vertices;
    private VertexBuffer vBuffer;
    private VertexArray vArray;
    private final IndexBuffer iBuffer;

    private final Configuration configuration;
    private final Shader shader;

    private int bufferedChar;

    /**
     * Constructs the text renderer
     */
    public TextRenderer() {
        this.vertices = MemoryUtil.memAllocFloat(MAX_TEXT_LENGTH * VERTICES_PER_CHAR * ELEMENTS_PER_VERTEX);
        this.initVertices();

        this.iBuffer = this.initIndices();

        this.configuration = Configuration.getInstance();
        this.shader = new Shader()
                .addVertexShader("/ui/text.vert")
                .addFragmentShader("/ui/text.frag")
                .compile();

        this.bufferedChar = 0;
    }

    /**
     * Initialize indices
     */
    private IndexBuffer initIndices() {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ShortBuffer indices = stack.mallocShort(MAX_TEXT_LENGTH * INDICES_PER_CHAR);
            for (int i = 0; i < MAX_TEXT_LENGTH; i++) {
                final int offset = i * INDICES_PER_CHAR;
                final int firstIndex = i * VERTICES_PER_CHAR;
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
     * Initialize vertices
     */
    private void initVertices() {
        this.vBuffer = new VertexBuffer(this.vertices, new VertexBufferParams()
                .usage(BufferUsage.DYNAMIC_DRAW)
                .element(new VertexElement(POSITION_INDEX, ELEMENTS_PER_POSITION))
                .element(new VertexElement(COORDINATES_INDEX, ELEMENTS_PER_COORDINATES))
                .element(new VertexElement(COLOR_INDEX, ELEMENTS_PER_COLOR)));
        this.vArray = new VertexArray();
        this.vArray.bind();
        this.vArray.attachVertexBuffer(this.vBuffer);
        this.vArray.unbind();
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.shader.destroy();
        MemoryUtil.memFree(this.vertices);
        this.iBuffer.destroy();
        this.vBuffer.destroy();
        this.vArray.destroy();
    }

    /**
     * Render a {@link String} on screen
     * <p>
     * The text is renderer at a given position with a given font, color and size.
     * The position and scale must be expressed in screen space values ranging from
     * 0 to 1. This allows text position and size to be consistent when resolution
     * changes. The origin is the bottom-left corner
     * <p>
     * For example, to render a text from the bottom left corner, with an height
     * equals to the height of the viewport. Position must be (0, 0) and scale = 1
     *
     * @param text     The text to render
     * @param font     The font to use to render the text
     * @param position The position of the text
     * @param scale    The scale of the text
     * @param color    The color of the text
     */
    public void render(final String text, final Font font, final Vector2 position, final float scale, final Color color) {
        final float aspectRatio = (float) this.configuration.getXResolution() / this.configuration.getYResolution();
        final float vScale = scale / font.getLineGap() * 2;
        final float hScale = vScale / aspectRatio;

        final Caret caret = new Caret(font.getLineGap() * vScale, position.getX() * 2 - HALF_SCREEN_SIZE,
                position.getY() * 2 - HALF_SCREEN_SIZE);

        final int textLength = text.length();
        for (int i = 0; i < textLength; i++) {
            final char c = text.charAt(i);
            if (c == '\n' || c == '\r') {
                caret.nextLine();
            } else {
                final Glyph glyph = font.getGlyph(c);
                if (Objects.nonNull(glyph)) {
                    this.generateGlyphVertices(glyph, caret, hScale, vScale, color);
                }
            }
        }
        this.renderText(font, scale);
    }

    /**
     * Generate the vertices for on glyph
     *
     * @param glyph  The glyph for which to generate the vertices
     * @param caret  The caret
     * @param hScale The horizontal scale
     * @param vScale The vertical scale
     * @param color  The color of the text
     */
    private void generateGlyphVertices(final Glyph glyph, final Caret caret, final float hScale, final float vScale, final Color color) {
        final TextureRegion region = glyph.getRegion();

        final float left = caret.isNewLine() ? caret.getX() : glyph.getXOffset() * hScale + caret.getX();
        final float right = left + (region.getRight() - region.getLeft()) * hScale;
        final float bottom = caret.getY() + glyph.getYOffset() * vScale;
        final float top = bottom + (region.getTop() - region.getBottom()) * vScale;

        final int vertexIndex = this.bufferedChar * VERTICES_PER_CHAR;
        this.updateVertex(vertexIndex, left, bottom, region.getLeft(), region.getBottom(), color);
        this.updateVertex(vertexIndex + 1, right, bottom, region.getRight(), region.getBottom(), color);
        this.updateVertex(vertexIndex + 2, left, top, region.getLeft(), region.getTop(), color);
        this.updateVertex(vertexIndex + 3, right, top, region.getRight(), region.getTop(), color);

        caret.advance(glyph.getXAdvance() * hScale);
        this.bufferedChar++;
    }

    /**
     * Update the data of one vertex
     *
     * @param vertexIndex The index of the vertex to update
     * @param x           The x position of the vertex
     * @param y           The y position of the vertex
     * @param u           The u coordinate of the vertex
     * @param v           The v coordinate of the vertex
     * @param color       The color of the vertex
     */
    private void updateVertex(final int vertexIndex, final float x, final float y, final float u, final float v, final Color color) {
        final int index = vertexIndex * ELEMENTS_PER_VERTEX;
        this.vertices.put(index, x);
        this.vertices.put(index + 1, y);
        this.vertices.put(index + 2, u);
        this.vertices.put(index + 3, v);
        this.vertices.put(index + 4, color.getRed());
        this.vertices.put(index + 5, color.getGreen());
        this.vertices.put(index + 6, color.getBlue());
        this.vertices.put(index + 7, color.getAlpha());
    }

    /**
     * Render the buffered text
     *
     * @param font  The font to use
     * @param scale The scale of the text
     */
    private void renderText(final Font font, final float scale) {
        this.vBuffer.bind();
        this.vBuffer.update(this.vertices);
        this.vBuffer.unbind();

        font.getBitmap().bind();
        this.shader.bind();
        this.shader.setUniform("thickness", 0.5f);
        this.shader.setUniform("smoothing", font.computeSmoothing(this.configuration.getYResolution() * scale));
        this.vArray.bind();
        this.iBuffer.bind();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        Engine.setBlendMode(BlendMode.TRANSPARENCY);
        GL11.glDrawElements(GL11.GL_TRIANGLES, this.bufferedChar * INDICES_PER_CHAR, this.iBuffer.getDataType().getGlCode(), 0);
        Engine.setBlendMode(BlendMode.NONE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        Shader.unbind();
        this.iBuffer.unbind();
        this.vArray.unbind();
        Texture.unbind();

        this.bufferedChar = 0;
    }
}
