package com.adrien.games.bagl.rendering.text;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.exception.ParseException;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.TextureRegion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text font used to render text.
 */
public class Font {

    private static final Logger log = LogManager.getLogger(Font.class);

    private static final Pattern HEADER_PAGE_PATTERN = Pattern.compile("^page.+file=\"(.+)\".*");
    private static final Pattern HEADER_COMMON_PATTERN = Pattern.compile("^common\\slineHeight=(\\d+)\\s+" +
            "base=(\\d+)\\s+scaleW=(\\d+)\\s+scaleH=(\\d+).*");
    private static final Pattern CHAR_LINE_PATTERN = Pattern.compile("^char\\sid=(\\d+)\\s+x=(\\d+)\\s+y=(\\d+)\\s+" +
            "width=(\\d+)\\s+height=(\\d+)\\s+xoffset=(-?\\d+)\\s+yoffset=(-?\\d+)\\s+xadvance=(-?\\d+)\\s+.*");

    private static final float FONT_SPREAD = 2.8f;
    private static final float SMOOTHING_FACTOR = 0.3f;

    private float lineGap;
    private float lineGapInPixels;
    private int pageWidth;
    private int pageHeight;
    private String atlasName;
    private final Map<Integer, Glyph> glyphs = new HashMap<>();
    private Texture bitmap;

    public Font(String filePath) {
        this.load(filePath);
    }

    private void load(String filePath) {
        final var file = new File(filePath);
        if (!file.exists()) {
            log.error("Font file '{}' does not exists.", filePath);
            throw new EngineException("Font file '" + filePath + "' does not exists.");
        }

        try (final var reader = new BufferedReader(new FileReader(file))) {
            this.parseHeader(reader);
            reader.lines().map(CHAR_LINE_PATTERN::matcher).filter(Matcher::matches).forEach(this::parseCharLine);
        } catch (final IOException | ParseException e) {
            log.error("Failed to parse font file '{}'.", filePath, e);
            throw new EngineException("Failed to parse font file '" + filePath + "'.", e);
        }

        this.bitmap = Texture.fromFile(file.getParentFile().getAbsolutePath() + File.separator + this.atlasName,
                true, TextureParameters.builder());
    }

    private void parseHeader(BufferedReader reader) throws IOException, ParseException {
        reader.readLine();//skip first line
        this.parseCommonHeader(this.checkMatch(HEADER_COMMON_PATTERN, reader.readLine()));
        this.parsePageHeader(this.checkMatch(HEADER_PAGE_PATTERN, reader.readLine()));
        reader.readLine();//skip the fourth line
    }

    private Matcher checkMatch(Pattern pattern, String line) throws ParseException {
        final var matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            throw new ParseException("Font file content is not correct");
        }
        return matcher;
    }

    private void parseCommonHeader(Matcher matcher) {
        this.pageWidth = Integer.parseInt(matcher.group(3));
        this.pageHeight = Integer.parseInt(matcher.group(4));
        this.lineGapInPixels = Float.parseFloat(matcher.group(1));
        this.lineGap = this.lineGapInPixels / this.pageHeight;
    }

    private void parsePageHeader(Matcher matcher) {
        this.atlasName = matcher.group(1);
    }

    private void parseCharLine(Matcher matcher) {
        final var id = Integer.parseInt(matcher.group(1));
        final var x = Float.parseFloat(matcher.group(2)) / this.pageWidth;
        final var y = Float.parseFloat(matcher.group(3)) / this.pageHeight;
        final var width = Float.parseFloat(matcher.group(4)) / this.pageWidth;
        final var height = Float.parseFloat(matcher.group(5)) / this.pageHeight;
        final var xOffset = Float.parseFloat(matcher.group(6)) / this.pageWidth;
        final var yOffset = Float.parseFloat(matcher.group(7)) / this.pageHeight;
        final var xAdvance = Float.parseFloat(matcher.group(8)) / this.pageWidth;

        this.glyphs.put(id, new Glyph(
                new TextureRegion(this.bitmap, x, 1f - y - height, x + width, 1f - y),
                xOffset, this.lineGap - height - yOffset, xAdvance));
    }

    /**
     * Computes the length a text rendered with this font.
     *
     * @param text The text to compute.
     * @return The length of the text in pixels.
     */
    public float getTextWidth(String text) {
        return (float) text.chars().boxed().map(i -> (char) i.intValue()).map(this::getGlyph).flatMap(Optional::stream)
                .mapToDouble(c -> c.getXAdvance() + c.getXOffset()).sum();
    }

    /**
     * Gets the glyph information for a given character.
     *
     * @param c The char to look for.
     * @return The glyph information as a {@link Glyph} or an empty optional if no glyph is found.
     */
    public Optional<Glyph> getGlyph(char c) {
        return Optional.ofNullable(this.glyphs.get((int) c));
    }

    /**
     * Computes the smoothing of the font.
     *
     * @param height The height of the rendered glyphs in pixels.
     * @return The smoothing factor to use at this given height.
     */
    public float computeSmoothing(float height) {
        final var pixelScaling = height / lineGapInPixels;
        return SMOOTHING_FACTOR / (FONT_SPREAD * pixelScaling);
    }

    public void destroy() {
        this.bitmap.destroy();
    }

    public Texture getBitmap() {
        return this.bitmap;
    }

    public float getLineGap() {
        return this.lineGap;
    }

    public float getLineGapInPixels() {
        return this.lineGapInPixels;
    }

}
