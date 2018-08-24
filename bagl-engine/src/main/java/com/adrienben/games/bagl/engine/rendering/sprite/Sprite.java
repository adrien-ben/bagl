package com.adrienben.games.bagl.engine.rendering.sprite;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import org.joml.Rectanglef;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.Objects;

/**
 * A sprite is a 2D renderable element.
 *
 * @author adrien
 */
public class Sprite {

    private static final Rectanglef DEFAULT_REGION = new Rectanglef(0.0f, 0.0f, 1.0f, 1.0f);

    private final Texture2D texture;
    private final float width;
    private final float height;
    private final Rectanglef region;
    private final Color color;
    private final Vector2f position;
    private float rotation;

    private Sprite(final Builder builder) {
        this.texture = Objects.requireNonNull(builder.texture);
        this.width = builder.width == 0.0f ? this.texture.getWidth() : builder.width;
        this.height = builder.height == 0.0f ? this.texture.getHeight() : builder.height;
        this.region = builder.region;
        this.color = builder.color;
        this.position = builder.position;
        this.rotation = builder.rotation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Texture2D getTexture() {
        return texture;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectanglef getRegion() {
        return region;
    }

    public Color getColor() {
        return color;
    }

    public Vector2fc getPosition() {
        return position;
    }

    public void setPosition(final Vector2fc position) {
        this.position.set(position);
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(final float rotation) {
        this.rotation = rotation;
    }

    /**
     * {@link Sprite} builder.
     * <p>
     * You can set the following fields :
     * <li>texture (required)</li>
     * <li>width (default = texture's width)</li>
     * <li>height (default = texture's height)</li>
     * <li>region (default = {0.0, 0.0, 1.0, 1.0} = the entire texture) : it is the region of the texture that will be rendered</li>
     * <li>color (default = WHITE) : it is a filter applied on top of the texture</li>
     * <li>position (default = {0.0, 0.0})</li>
     * <li>rotation</li>
     */
    public static class Builder {

        private Texture2D texture;
        private float width;
        private float height;
        private Rectanglef region = new Rectanglef(DEFAULT_REGION);
        private Color color = Color.WHITE;
        private Vector2f position = new Vector2f();
        private float rotation;

        private Builder() {
        }

        public Sprite build() {
            return new Sprite(this);
        }

        public Builder texture(final Texture2D texture) {
            this.texture = Objects.requireNonNull(texture);
            return this;
        }

        public Builder width(final float width) {
            this.width = width;
            return this;
        }

        public Builder height(final float height) {
            this.height = height;
            return this;
        }

        public Builder region(final Rectanglef region) {
            this.region = Objects.requireNonNull(region);
            return this;
        }

        public Builder color(final Color color) {
            this.color = Objects.requireNonNull(color);
            return this;
        }

        public Builder position(final Vector2f position) {
            this.position = Objects.requireNonNull(position);
            return this;
        }

        public Builder rotation(final float rotation) {
            this.rotation = rotation;
            return this;
        }
    }
}
