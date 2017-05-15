package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexDescription;
import com.adrien.games.bagl.rendering.vertex.VertexElement;
import com.adrien.games.bagl.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Text sample. Implementation of the signed distance field algorithm from Valve.
 */
public class TextSample implements Game {

    private static final Logger log = LogManager.getLogger(TextSample.class);

    private static final String TITLE = "Text Sample";

    private static final String TEST_STRING = "Hello {World} :)";
    private static final String FONT_RESOURCE_FORLDER = "/fonts/";
    private static final String FONT_NAME = "segoe";
    private static final Color TEXT_COLOR = Color.DARK_GRAY;
    private static final float TEXT_SCALE = 2.2f;

    private static final Pattern HEADER_COMMON_PATTERN = Pattern.compile("^common\\slineHeight=(\\d+)\\s+base=(\\d+)\\s+" +
            "scaleW=(\\d+)\\s+scaleH=(\\d+).*");
    private static final Pattern HEADER_PAGE_PATTERN = Pattern.compile("^page.+file=\"(.+)\".*");
    private static final Pattern CHAR_LINE_PATTERN = Pattern.compile("^char\\sid=(\\d+)\\s+x=(\\d+)\\s+y=(\\d+)\\s+" +
            "width=(\\d+)\\s+height=(\\d+)\\s+xoffset=(-?\\d+)\\s+yoffset=(-?\\d+)\\s+xadvance=(-?\\d+)\\s+.*");
    private static final int VERTEX_PER_CHAR = 4;
    private static final int INDEX_PER_CHAR = 6;

    private float aspectRatio;

    private float lineHeight;
    private int pageWidth;
    private int pageHeight;
    private String pageFile;
    private Map<Integer, Glyph> glyphs = new HashMap<>();
    //TODO: load texture from thr path found in the font file.
    private Texture texture;
    private Shader shader;

    @Override
    public void init() {
        Engine.setClearColor(Color.CORNFLOWER_BLUE);

        this.aspectRatio = (float)Configuration.getInstance().getXResolution()/Configuration.getInstance().getYResolution();

        this.loadFontFile();

        this.shader = new Shader().addVertexShader("sdf_text.vert").addFragmentShader("sdf_text.frag").compile();
    }

    @Override
    public void destroy() {
        this.texture.destroy();
    }

    private void loadFontFile() {
        try(final Stream<String> lines = Files.lines(Paths.get(FileUtils.getResourceAbsolutePath(FONT_RESOURCE_FORLDER + FONT_NAME + "/" + FONT_NAME + ".fnt")))) {
            lines.forEach(this::parseLine);
        } catch (IOException e) {
            log.error("Failed to load font file", e);
        }

        this.texture = new Texture(FileUtils.getResourceAbsolutePath(FONT_RESOURCE_FORLDER + FONT_NAME + "/" + this.pageFile),
                new TextureParameters().format(Format.ALPHA8));
    }

    private void parseLine(String line) {
        final Matcher headerCommonMatcher = HEADER_COMMON_PATTERN.matcher(line);
        final Matcher headerPageMatcher = HEADER_PAGE_PATTERN.matcher(line);
        final Matcher charLineMatcher = CHAR_LINE_PATTERN.matcher(line);
        if(headerCommonMatcher.matches()) {
            this.processCommonHeader(headerCommonMatcher);
        } else if(headerPageMatcher.matches()) {
            this.processPageHeader(headerPageMatcher);
        } else if(charLineMatcher.matches()){
            this.processCharLine(charLineMatcher);
        }
    }

    private void processCommonHeader(Matcher matcher) {
        this.pageWidth = Integer.parseInt(matcher.group(3));
        this.pageHeight = Integer.parseInt(matcher.group(4));
        this.lineHeight = Float.parseFloat(matcher.group(1)) / this.pageHeight;
    }

    private void processPageHeader(Matcher matcher) {
        this.pageFile = matcher.group(1);
    }

    private void processCharLine(Matcher matcher) {
        final int id = Integer.parseInt(matcher.group(1));
        final float x = Float.parseFloat(matcher.group(2)) / this.pageWidth;
        final float y = Float.parseFloat(matcher.group(3)) / this.pageHeight;
        final float width = Float.parseFloat(matcher.group(4)) / this.pageWidth;
        final float height = Float.parseFloat(matcher.group(5)) / this.pageHeight;
        final float xOffset = Float.parseFloat(matcher.group(6)) / this.pageWidth;
        final float yOffset = Float.parseFloat(matcher.group(7)) / this.pageHeight;
        final float xAdvance = Float.parseFloat(matcher.group(8)) / this.pageWidth;

        this.glyphs.put(id, new Glyph(id, x, 1f - y - height, width, height, xOffset, this.lineHeight - height - yOffset, xAdvance));
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

            final Glyph glyph = this.glyphs.get(TEST_STRING.codePointAt(i));
            if(Objects.nonNull(glyph)) {
                final float left = glyph.getxOffset()*TEXT_SCALE + advance;
                final float right = left + glyph.getWidth()*TEXT_SCALE;
                final float bottom = baseline + glyph.getyOffset()*TEXT_SCALE;
                final float top = bottom + glyph.getHeight()*this.aspectRatio*TEXT_SCALE;

                vertices[verticesIndex++] = new TextVertex(new Vector2(left,  bottom), new Vector2(glyph.getX(), glyph.getY()), TEXT_COLOR);
                vertices[verticesIndex++] = new TextVertex(new Vector2(right, bottom), new Vector2(glyph.getX() + glyph.getWidth(), glyph.getY()), TEXT_COLOR);
                vertices[verticesIndex++] = new TextVertex(new Vector2(left, top), new Vector2(glyph.getX(), glyph.getY() + glyph.getHeight()), TEXT_COLOR);
                vertices[verticesIndex++] = new TextVertex(new Vector2(right, top), new Vector2(glyph.getX() + glyph.getWidth(), glyph.getY() + glyph.getHeight()), TEXT_COLOR);

                indices[indicesIndex++] = i*VERTEX_PER_CHAR;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 1;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 2;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 2;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 1;
                indices[indicesIndex++] = i*VERTEX_PER_CHAR + 3;

                advance += glyph.getxAdvance()*TEXT_SCALE;
            }
        }

        final VertexBuffer vertexBuffer = new VertexBuffer(TextVertex.VERTEX_DESCRIPTION, BufferUsage.STATIC_DRAW, vertices);
        final IndexBuffer indexBuffer = new IndexBuffer(BufferUsage.STATIC_DRAW, indices);

        this.texture.bind();
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
     * Glyph class
     */
    private static class Glyph {

        private final int id;
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final float xOffset;
        private final float yOffset;
        private final float xAdvance;

        public Glyph(int id, float x, float y, float width, float height, float xOffset, float yOffset, float xAdvance) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.xAdvance = xAdvance;
        }

        @Override
        public String toString() {
            return id + " " + x + " " + y + " " + width + " " + height + " " + xOffset + " " + yOffset + " " + xAdvance;
        }

        public int getId() {
            return id;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        public float getxOffset() {
            return xOffset;
        }

        public float getyOffset() {
            return yOffset;
        }

        public float getxAdvance() {
            return xAdvance;
        }

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
