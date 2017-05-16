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
    private static final int VERTEX_PER_CHAR = 4;
    private static final int INDEX_PER_CHAR = 6;
    private static final float HALF_SCREEN_SIZE = 1f;

    private Configuration configuration;
    private Shader shader;

    public TextRenderer() {
        this.configuration = Configuration.getInstance();
        this.shader = new Shader().addVertexShader(TEXT_VERTEX_SHADER).addFragmentShader(TEXT_FRAGMENT_SHADER).compile();
    }

    /**
     * Release resources.
     */
    public void destroy() {
        this.shader.destroy();
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
        final TextVertex[] vertices = new TextVertex[textLength*VERTEX_PER_CHAR];
        for(int i = 0 ; i < textLength; i++) {

            int verticesIndex = charCount*VERTEX_PER_CHAR;

            final char c = text.charAt(i);
            if(c == '\n' || c == '\r') {
                caret.nextLine();
            } else {
                final Glyph glyph = font.getGlyph(c);
                if(Objects.nonNull(glyph)) {
                    final TextureRegion region = glyph.getRegion();

                    final float left = caret.isNewLine() ? caret.getX() : glyph.getXOffset()*hScale + caret.getX();
                    final float right = left + (region.getRight() - region.getLeft())*hScale;
                    final float bottom = caret.getY() + glyph.getYOffset();
                    final float top = bottom + (region.getTop() - region.getBottom())*vScale;

                    vertices[verticesIndex++] = new TextVertex(new Vector2(left,  bottom),
                            new Vector2(region.getLeft(), region.getBottom()), color);
                    vertices[verticesIndex++] = new TextVertex(new Vector2(right, bottom),
                            new Vector2(region.getRight(), region.getBottom()), color);
                    vertices[verticesIndex++] = new TextVertex(new Vector2(left, top),
                            new Vector2(region.getLeft(), region.getTop()), color);
                    vertices[verticesIndex] = new TextVertex(new Vector2(right, top),
                            new Vector2(region.getRight(), region.getTop()), color);

                    caret.advance(glyph.getXAdvance()*hScale);
                    charCount++;
                }
            }
        }

        final IndexBuffer indexBuffer = this.generateIndexBuffer(charCount);

        final VertexBuffer vertexBuffer = new VertexBuffer(TextVertex.VERTEX_DESCRIPTION, BufferUsage.STATIC_DRAW,
                charCount*VERTEX_PER_CHAR);
        vertexBuffer.setData(vertices, charCount*VERTEX_PER_CHAR);

        font.getBitmap().bind();
        this.shader.bind();
        this.shader.setUniform("thickness", 0.5f);
        this.shader.setUniform("smoothing", font.computeSmoothing(this.configuration.getYResolution()*scale));
        vertexBuffer.bind();
        indexBuffer.bind();

        Engine.setBlendMode(BlendMode.TRANSPARENCY);
        GL11.glDrawElements(GL11.GL_TRIANGLES, charCount*INDEX_PER_CHAR, GL11.GL_UNSIGNED_INT, 0);
        Engine.setBlendMode(BlendMode.NONE);

        Shader.unbind();
        IndexBuffer.unbind();
        VertexBuffer.unbind();
        Texture.unbind();

        vertexBuffer.destroy();
        indexBuffer.destroy();
    }

    private IndexBuffer generateIndexBuffer(int size) {
        final int[] indices = new int[size*INDEX_PER_CHAR];
        for(int i = 0 ; i < size; i++) {
            int indicesIndex = i*INDEX_PER_CHAR;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR + 1;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR + 2;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR + 2;
            indices[indicesIndex++] = i*VERTEX_PER_CHAR + 1;
            indices[indicesIndex] = i*VERTEX_PER_CHAR + 3;
        }
        return new IndexBuffer(BufferUsage.STATIC_DRAW, indices);
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
