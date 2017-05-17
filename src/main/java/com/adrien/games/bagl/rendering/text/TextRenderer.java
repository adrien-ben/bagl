package com.adrien.games.bagl.rendering.text;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexDescription;
import com.adrien.games.bagl.rendering.vertex.VertexElement;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

/**
 * Text renderer.
 */
public class TextRenderer {

    private static final String TEXT_VERTEX_SHADER = "text.vert";
    private static final String TEXT_FRAGMENT_SHADER = "text.frag";
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final int VERTEX_PER_CHAR = 4;
    private static final int INDEX_PER_CHAR = 6;
    private static final float HALF_SCREEN_SIZE = 1f;

    private TextVertex[] buffer;
    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;

    private Configuration configuration;
    private Shader shader;

    public TextRenderer() {
        this.indexBuffer = initIndexBuffer();
        this.vertexBuffer = initVertexBuffer();
        this.buffer = initVertices();

        this.configuration = Configuration.getInstance();
        this.shader = new Shader().addVertexShader(TEXT_VERTEX_SHADER).addFragmentShader(TEXT_FRAGMENT_SHADER).compile();
    }

    private static TextVertex[] initVertices() {
        final TextVertex[] vertices = new TextVertex[MAX_TEXT_LENGTH*VERTEX_PER_CHAR];
        for(int i = 0; i < MAX_TEXT_LENGTH*VERTEX_PER_CHAR; i++) {
            vertices[i] = new TextVertex(new Vector2(), new Vector2(), new Color(1, 1, 1));
        }
        return vertices;
    }

    private static VertexBuffer initVertexBuffer() {
        return new VertexBuffer(TextVertex.VERTEX_DESCRIPTION, BufferUsage.DYNAMIC_DRAW,
                MAX_TEXT_LENGTH*VERTEX_PER_CHAR);
    }

    private static IndexBuffer initIndexBuffer() {
        final int[] indices = new int[MAX_TEXT_LENGTH*INDEX_PER_CHAR];
        for(int i = 0 ; i < MAX_TEXT_LENGTH; i++) {
            int indicesIndex = i*INDEX_PER_CHAR;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR + 1;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR + 2;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR + 2;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR + 1;
            indices[indicesIndex] = i*VERTEX_PER_CHAR + 3;
        }
        return new IndexBuffer(BufferUsage.DYNAMIC_DRAW, indices);
    }

    /**
     * Release resources.
     */
    public void destroy() {
        this.shader.destroy();
        this.vertexBuffer.destroy();
        this.indexBuffer.destroy();
    }

    /**
     * <p>Renders a {@link String} on screen.
     * <p>The text is renderer at a given position with a given font, color and size.
     * The position and scale must be expressed in screen space values ranging from
     * 0 to 1. This allows text position and size to be consistent when resolution
     * changes. The origin is the bottom-left corner.
     * <p>For example, to render a text from the bottom left corner, with an height
     * equals to the height of the viewport. Position must be (0, 0) and scale = 1.
     * @param text The text to render.
     * @param font The font to use to render the text.
     * @param position The position of the text.
     * @param scale The scale of the text.
     * @param color The color of the text.
     */
    public void render(String text, Font font, Vector2 position, float scale, Color color) {
        final float aspectRatio = (float)this.configuration.getXResolution()/this.configuration.getYResolution();
        final float vScale = scale/font.getLineGap()*2;
        final float hScale = vScale/aspectRatio;

        final Caret caret = new Caret(font.getLineGap()*vScale, position.getX()*2 - HALF_SCREEN_SIZE,
                position.getY()*2 - HALF_SCREEN_SIZE);

        int charCount = 0;
        final int textLength = text.length();
        for(int i = 0 ; i < textLength; i++) {

            final char c = text.charAt(i);
            if(c == '\n' || c == '\r') {
                caret.nextLine();
            } else {
                final Glyph glyph = font.getGlyph(c);
                if(Objects.nonNull(glyph)) {
                    final TextureRegion region = glyph.getRegion();

                    final float left = caret.isNewLine() ? caret.getX() : glyph.getXOffset()*hScale + caret.getX();
                    final float right = left + (region.getRight() - region.getLeft())*hScale;
                    final float bottom = caret.getY() + glyph.getYOffset()*vScale;
                    final float top = bottom + (region.getTop() - region.getBottom())*vScale;

                    final TextVertex v0 = this.buffer[charCount*VERTEX_PER_CHAR];
                    final TextVertex v1 = this.buffer[charCount*VERTEX_PER_CHAR + 1];
                    final TextVertex v2 = this.buffer[charCount*VERTEX_PER_CHAR + 2];
                    final TextVertex v3 = this.buffer[charCount*VERTEX_PER_CHAR + 3];

                    v0.position.setX(left);
                    v0.position.setY(bottom);
                    v0.coords.setX(region.getLeft());
                    v0.coords.setY(region.getBottom());
                    v0.color.set(color);

                    v1.position.setX(right);
                    v1.position.setY(bottom);
                    v1.coords.setX(region.getRight());
                    v1.coords.setY(region.getBottom());
                    v1.color.set(color);

                    v2.position.setX(left);
                    v2.position.setY(top);
                    v2.coords.setX(region.getLeft());
                    v2.coords.setY(region.getTop());
                    v2.color.set(color);

                    v3.position.setX(right);
                    v3.position.setY(top);
                    v3.coords.setX(region.getRight());
                    v3.coords.setY(region.getTop());
                    v3.color.set(color);

                    caret.advance(glyph.getXAdvance()*hScale);
                    charCount++;
                }
            }
        }

        this.vertexBuffer.setData(this.buffer, charCount*VERTEX_PER_CHAR);

        font.getBitmap().bind();
        this.shader.bind();
        this.shader.setUniform("thickness", 0.5f);
        this.shader.setUniform("smoothing", font.computeSmoothing(this.configuration.getYResolution()*scale));
        this.vertexBuffer.bind();
        this.indexBuffer.bind();

        Engine.setBlendMode(BlendMode.TRANSPARENCY);
        GL11.glDrawElements(GL11.GL_TRIANGLES, charCount*INDEX_PER_CHAR, GL11.GL_UNSIGNED_INT, 0);
        Engine.setBlendMode(BlendMode.NONE);

        Shader.unbind();
        IndexBuffer.unbind();
        VertexBuffer.unbind();
        Texture.unbind();
    }

    /**
     * Text vertex.
     */
    private static class TextVertex implements Vertex {

        static final VertexDescription VERTEX_DESCRIPTION = new VertexDescription(new VertexElement[]{
                new VertexElement(0, 2, 0), new VertexElement(1, 2, 2),
                new VertexElement(2, 4, 4)});

        final Vector2 position;
        final Vector2 coords;
        final Color color;

        TextVertex(Vector2 position, Vector2 coords, Color color) {
            this.position = position;
            this.coords = coords;
            this.color = color;
        }

        @Override
        public float[] getData() {
            return new float[]{this.position.getX(), this.position.getY(), this.coords.getX(), this.coords.getY(),
                    this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.color.getAlpha()};
        }

    }

}
