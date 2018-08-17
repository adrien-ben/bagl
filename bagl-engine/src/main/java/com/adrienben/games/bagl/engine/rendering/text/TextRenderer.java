package com.adrienben.games.bagl.engine.rendering.text;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.engine.rendering.renderer.Renderer;
import com.adrienben.games.bagl.opengl.BlendMode;
import com.adrienben.games.bagl.opengl.BufferUsage;
import com.adrienben.games.bagl.opengl.OpenGL;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.vertex.*;
import org.joml.Vector2fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * Text renderer
 *
 * @author adrien
 */
public class TextRenderer implements Renderer<Text> {

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
        this.shader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/ui/text.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/ui/text.frag"))
                .build();

        this.bufferedChar = 0;
    }

    /**
     * Initialize indices
     */
    private IndexBuffer initIndices() {
        try (final var stack = MemoryStack.stackPush()) {
            final var indices = stack.mallocShort(MAX_TEXT_LENGTH * INDICES_PER_CHAR);
            for (var i = 0; i < MAX_TEXT_LENGTH; i++) {
                final var offset = i * INDICES_PER_CHAR;
                final var firstIndex = i * VERTICES_PER_CHAR;
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
        this.vBuffer = new VertexBuffer(this.vertices, VertexBufferParams.builder()
                .usage(BufferUsage.DYNAMIC_DRAW)
                .element(new VertexElement(POSITION_INDEX, ELEMENTS_PER_POSITION))
                .element(new VertexElement(COORDINATES_INDEX, ELEMENTS_PER_COORDINATES))
                .element(new VertexElement(COLOR_INDEX, ELEMENTS_PER_COLOR))
                .build());
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
     * Render {@code text}.
     */
    @Override
    public void render(final Text text) {
        render(text.getValue(), text.getFont(), text.getPosition(), text.getScale(), text.getColor());
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
    private void render(final String text, final Font font, final Vector2fc position, final float scale, final Color color) {
        final var aspectRatio = (float) this.configuration.getXResolution() / this.configuration.getYResolution();
        final var vScale = scale / font.getLineGap() * 2;
        final var hScale = vScale / aspectRatio;

        final var caret = new Caret(font.getLineGap() * vScale, position.x() * 2 - HALF_SCREEN_SIZE,
                position.y() * 2 - HALF_SCREEN_SIZE);

        final var textLength = text.length();
        for (var i = 0; i < textLength; i++) {
            final var c = text.charAt(i);
            if (c == '\n' || c == '\r') {
                caret.nextLine();
            } else {
                font.getGlyph(c).ifPresent(glyph -> this.generateGlyphVertices(glyph, caret, hScale, vScale, color));
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
        final var region = glyph.getRegion();

        final var left = caret.isNewLine() ? caret.getX() : glyph.getXOffset() * hScale + caret.getX();
        final var right = left + (region.getRight() - region.getLeft()) * hScale;
        final var bottom = caret.getY() + glyph.getYOffset() * vScale;
        final var top = bottom + (region.getTop() - region.getBottom()) * vScale;

        final var vertexIndex = this.bufferedChar * VERTICES_PER_CHAR;
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
        final var index = vertexIndex * ELEMENTS_PER_VERTEX;
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
        OpenGL.setBlendMode(BlendMode.TRANSPARENCY);
        GL11.glDrawElements(GL11.GL_TRIANGLES, this.bufferedChar * INDICES_PER_CHAR, this.iBuffer.getDataType().getGlCode(), 0);
        OpenGL.setBlendMode(BlendMode.NONE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        Shader.unbind();
        this.iBuffer.unbind();
        this.vArray.unbind();
        font.getBitmap().unbind();

        this.bufferedChar = 0;
    }
}
