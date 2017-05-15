package com.adrien.games.bagl.rendering.text;

import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
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
 * Text font used to render text.
 *
 */
public class Font {

    private static final Logger log = LogManager.getLogger(Font.class);

    private static final Pattern HEADER_PAGE_PATTERN = Pattern.compile("^page.+file=\"(.+)\".*");
    private static final Pattern HEADER_COMMON_PATTERN = Pattern.compile("^common\\slineHeight=(\\d+)\\s+" +
            "base=(\\d+)\\s+scaleW=(\\d+)\\s+scaleH=(\\d+).*");
    private static final Pattern CHAR_LINE_PATTERN = Pattern.compile("^char\\sid=(\\d+)\\s+x=(\\d+)\\s+y=(\\d+)\\s+" +
            "width=(\\d+)\\s+height=(\\d+)\\s+xoffset=(-?\\d+)\\s+yoffset=(-?\\d+)\\s+xadvance=(-?\\d+)\\s+.*");

    private float lineHeight;
    private int pageWidth;
    private int pageHeight;
    private String atlasName;
    private Map<Integer, Glyph> glyphs = new HashMap<>();
    private Texture bitmap;

    public Font(String filePath) {
        this.load(filePath);
    }

    private void load(String filePath) {
        final File file = new File(filePath);
        if(!file.exists()) {
            log.error("Font file '{}' does not exists.", filePath);
            throw new RuntimeException("Font file '" + filePath + "' does not exists.");
        }

        try(final Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.forEach(this::parseLine);
        } catch (IOException e) {
            log.error("Failed to load font file", e);
        }

        this.bitmap = new Texture(file.getParentFile().getAbsolutePath() + File.separator + this.atlasName,
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
        this.atlasName = matcher.group(1);
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

        this.glyphs.put(id, new Glyph(new TextureRegion(this.bitmap, x, 1f - y - height, x + width, 1f - y),
                width, height, xOffset, this.lineHeight - height - yOffset, xAdvance, (char)id));
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
        return this.glyphs.get((int)c);
    }

    public void destroy() {
        this.bitmap.destroy();
    }

    public int getSize() {
        return 0;
    }

    public Texture getBitmap() {
        return this.bitmap;
    }

    public int getAscent() {
        return 0;
    }

    public int getDescent() {
        return 0;
    }

    public int getLineGap() {
        return 0;
    }

}
