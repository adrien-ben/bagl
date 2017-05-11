package com.adrien.games.bagl.rendering.text;

import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import com.adrien.games.bagl.utils.FileUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Text font used to render text.
 * @author Adrien
 *
 */
public class Font {

    private static final int BMP_WIDTH = 512;
    private static final int BMP_HEIGHT = 512;
    private static final int FIRST_CHAR = 32;
    private static final int CHAR_COUNT = 96;

    private final int size;
    private Texture bitmap;
    private final Glyph[] glyphs = new Glyph[CHAR_COUNT];
    private int ascent;
    private int descent;
    private int lineGap;

    public Font(String filePath, int size) {
        this.size = size;
        this.load(filePath);
    }

    private void load(String filePath) {
        ByteBuffer ttf = FileUtils.loadAsByteBuffer(filePath);

        STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(CHAR_COUNT);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BMP_WIDTH * BMP_HEIGHT);
        STBTruetype.stbtt_BakeFontBitmap(ttf, this.size, bitmap, BMP_WIDTH, BMP_HEIGHT, FIRST_CHAR, cdata);
        this.bitmap = new Texture(BMP_WIDTH, BMP_HEIGHT, bitmap, new TextureParameters().format(Format.ALPHA8));

        STBTTFontinfo infos = STBTTFontinfo.malloc();
        STBTruetype.stbtt_InitFont(infos, ttf);

        this.getFontVMetrics(infos);
        this.getCharInfos(infos, cdata);

        infos.free();
        cdata.free();
    }

    private void getFontVMetrics(STBTTFontinfo infos) {
        float scale = STBTruetype.stbtt_ScaleForPixelHeight(infos, this.size);
        int[] ascent = new int[1];
        int[] descent = new int[1];
        int[] lineGap = new int[1];
        STBTruetype.stbtt_GetFontVMetrics(infos, ascent, descent, lineGap);
        this.ascent = (int)(scale*ascent[0]);
        this.descent = (int)(scale*descent[0]);
        this.lineGap = (int)(scale*lineGap[0]);
    }

    private void getCharInfos(STBTTFontinfo infos, STBTTBakedChar.Buffer cdata) {
        float[] xpos = {0};
        float[] ypos = {0};
        STBTTAlignedQuad q = STBTTAlignedQuad.malloc();
        for(int i = 0; i < CHAR_COUNT; i++) {
            int codePoint = i + FIRST_CHAR;
            STBTTBakedChar charBuffer = cdata.get(i);
            STBTruetype.stbtt_GetBakedQuad(cdata, BMP_WIDTH, BMP_HEIGHT, i, xpos, ypos, q, true);
            TextureRegion region = new TextureRegion(this.bitmap, q.s0(), q.t1(), q.s1(), q.t0());
            glyphs[i] = new Glyph(region, q.x1() - q.x0(), q.y1() - q.y0(), charBuffer.xoff(),
                    charBuffer.yoff(), charBuffer.xadvance(), (char)codePoint);
        }
        q.free();
    }

    /**
     * Computes the length a text rendered with this font.
     * @param text The text to compute.
     * @return The length of the text in pixels.
     */
    public float getTextWidth(String text) {
        return (float)text.chars().boxed().map(i -> (char)i.intValue()).map(this::getGlyph).filter(Objects::nonNull)
                .mapToDouble(c -> c.getXAdvance() + c.getXOffset()).sum();
    }

    /**
     * Gets the glyph information for a given character.
     * @param c The char to look for.
     * @return The glyph information as a {@link Glyph} or null if no glyph is found.
     */
    public Glyph getGlyph(char c) {
        int index = (int)c - FIRST_CHAR;
        return (index < 0 || index >= CHAR_COUNT) ?  null : this.glyphs[index];
    }

    public void destroy() {
        this.bitmap.destroy();
    }

    public int getSize() {
        return size;
    }

    public Texture getBitmap() {
        return bitmap;
    }

    public int getAscent() {
        return ascent;
    }

    public int getDescent() {
        return descent;
    }

    public int getLineGap() {
        return lineGap;
    }

}
