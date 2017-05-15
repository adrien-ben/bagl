package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.text.Glyph;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexDescription;
import com.adrien.games.bagl.rendering.vertex.VertexElement;
import com.adrien.games.bagl.utils.FileUtils;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

/**
 * Text sample. Implementation of the signed distance field algorithm from Valve.
 */
public class TextSample implements Game {

    private static final String TITLE = "Text Sample";

    private static final String TEST_STRING = "Hello {World} :)";
    private static final Color TEXT_COLOR = Color.DARK_GRAY;
    private static final float TEXT_SCALE = 2.2f;

    private static final int VERTEX_PER_CHAR = 4;
    private static final int INDEX_PER_CHAR = 6;

    private float aspectRatio;

    private Shader shader;
    private Font font;

    @Override
    public void init() {
        Engine.setClearColor(Color.CORNFLOWER_BLUE);
        this.aspectRatio = (float)Configuration.getInstance().getXResolution()/Configuration.getInstance().getYResolution();
        this.shader = new Shader().addVertexShader("sdf_text.vert").addFragmentShader("sdf_text.frag").compile();
        this.font = new Font(FileUtils.getResourceAbsolutePath("/fonts/segoe/segoe.fnt"));
    }

    @Override
    public void destroy() {
        this.shader.destroy();
        this.font.destroy();
    }

    @Override
    public void update(Time time) {
    }

    @Override
    public void render() {
        final int textLength = TEST_STRING.length();
        final TextVertex[] vertices = new TextVertex[textLength*VERTEX_PER_CHAR];
        final int[] indices = new int[textLength*INDEX_PER_CHAR];
        float advance = -1f;
        for(int i = 0 ; i < textLength; i++) {
            int verticesIndex = i*VERTEX_PER_CHAR;
            int indicesIndex = i*INDEX_PER_CHAR;

            final float baseline = -1f;

            final Glyph glyph = this.font.getGlyph(TEST_STRING.charAt(i));
            if(Objects.nonNull(glyph)) {
                final float left = glyph.getXOffset()*TEXT_SCALE + advance;
                final float right = left + glyph.getWidth()*TEXT_SCALE;
                final float bottom = baseline + glyph.getYOffset()*TEXT_SCALE;
                final float top = bottom + glyph.getHeight()*this.aspectRatio*TEXT_SCALE;

                final TextureRegion region = glyph.getRegion();

                vertices[verticesIndex++] = new TextVertex(new Vector2(left,  bottom), new Vector2(region.getLeft(), region.getBottom()), TEXT_COLOR);
                vertices[verticesIndex++] = new TextVertex(new Vector2(right, bottom), new Vector2(region.getRight(), region.getBottom()), TEXT_COLOR);
                vertices[verticesIndex++] = new TextVertex(new Vector2(left, top), new Vector2(region.getLeft(), region.getTop()), TEXT_COLOR);
                vertices[verticesIndex++] = new TextVertex(new Vector2(right, top), new Vector2(region.getRight(), region.getTop()), TEXT_COLOR);

                indices[indicesIndex++] = i*VERTEX_PER_CHAR;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 1;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 2;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 2;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 1;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 3;

                advance += glyph.getXAdvance()*TEXT_SCALE;
            }
        }

        final VertexBuffer vertexBuffer = new VertexBuffer(TextVertex.VERTEX_DESCRIPTION, BufferUsage.STATIC_DRAW, vertices);
        final IndexBuffer indexBuffer = new IndexBuffer(BufferUsage.STATIC_DRAW, indices);

        this.font.getBitmap().bind();
        this.shader.bind();
        vertexBuffer.bind();
        indexBuffer.bind();

        Engine.setBlendMode(BlendMode.TRANSPARENCY);
        GL11.glDrawElements(GL11.GL_TRIANGLES, indices.length, GL11.GL_UNSIGNED_INT, 0);
        Engine.setBlendMode(BlendMode.NONE);

        Shader.unbind();
        IndexBuffer.unbind();
        VertexBuffer.unbind();
        Texture.unbind();

        vertexBuffer.destroy();
        indexBuffer.destroy();
    }

    /**
     * Text vertex.
     */
    private static class TextVertex implements Vertex {

        static final VertexDescription VERTEX_DESCRIPTION = new VertexDescription(new VertexElement[]{
                new VertexElement(0, 2, 0), new VertexElement(1, 2, 2),
                new VertexElement(2, 4, 4)});

        private final Vector2 position;
        private final Vector2 coords;
        private final Color color;

        public TextVertex(Vector2 position, Vector2 coords, Color color) {
            this.position = position;
            this.coords = coords;
            this.color = color;
        }

        @Override
        public float[] getData() {
            return new float[]{this.position.getX(), this.position.getY(), this.coords.getX(), this.coords.getY(),
                this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.color.getAlpha()};
        }

        public Vector2 getPosition() {
            return position;
        }

        public Vector2 getCoords() {
            return coords;
        }

        public Color getColor() {
            return color;
        }

    }

    public static void main(String[] args) {
        new Engine(new TextSample(), TITLE).start();
    }
}
