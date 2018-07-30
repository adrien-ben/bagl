package com.adrien.games.bagl.engine.rendering.text;

import com.adrien.games.bagl.core.Color;
import org.joml.Vector2f;
import org.joml.Vector2fc;

/**
 * Represent a renderable text.
 * <p>
 * A text as a value (the string to render), a font with which to render the text,
 * a 2D position (the position of the bottom left corner of the text), a scale and
 * a color.
 * <p>
 * Position and scale are expressed in percentage. For the position (0, 0) is the
 * bottom left corner of the screen and (1, 1) the top left corner. For the scale 1
 * means that the height of the text will bi equal to the height of the screen.
 *
 * @author adrien
 */
public class Text {

    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final float DEFAULT_SCALE = 1f;
    private static final float DEFAULT_X = 0f;
    private static final float DEFAULT_Y = 0f;

    private final Vector2f position;
    private String value;
    private Font font;
    private float scale;
    private Color color;

    private Text(final String value, final Font font, final float x, final float y, final float scale, final Color color) {
        this.value = value;
        this.font = font;
        this.position = new Vector2f(x, y);
        this.scale = scale;
        this.color = color;
    }

    /**
     * Create a new text.
     */
    public static Text create(final String value, final Font font, final float x, final float y, final float scale, final Color color) {
        return new Text(value, font, x, y, scale, color);
    }

    /**
     * Create a new text positioned at ({@value DEFAULT_X}, {@value DEFAULT_Y}) and with a scale of {@value DEFAULT_SCALE}.
     */
    public static Text create(final String value, final Font font, final Color color) {
        return new Text(value, font, DEFAULT_X, DEFAULT_Y, DEFAULT_SCALE, color);
    }

    /**
     * Create a new text positioned at ({@value DEFAULT_X}, {@value DEFAULT_Y}) with a scale of {@value DEFAULT_SCALE} and
     * with colored in {@code DEFAULT_COLOR}.
     */
    public static Text create(final String value, final Font font) {
        return new Text(value, font, DEFAULT_X, DEFAULT_Y, DEFAULT_SCALE, DEFAULT_COLOR);
    }

    public Text setX(final float x) {
        position.set(x, position.y());
        return this;
    }

    public Text setY(final float y) {
        position.set(position.x(), y);
        return this;
    }

    public Text setXY(final float x, final float y) {
        position.set(x, y);
        return this;
    }

    public String getValue() {
        return value;
    }

    public Text setValue(final String value) {
        this.value = value;
        return this;
    }

    public Font getFont() {
        return font;
    }

    public Text setFont(final Font font) {
        this.font = font;
        return this;
    }

    public Vector2fc getPosition() {
        return position;
    }

    public float getScale() {
        return scale;
    }

    public Text setScale(final float scale) {
        this.scale = scale;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public Text setColor(final Color color) {
        this.color = color;
        return this;
    }
}
