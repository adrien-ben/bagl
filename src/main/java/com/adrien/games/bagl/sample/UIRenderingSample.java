package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.shape.UIRenderer;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * {@link UIRenderer} sample class.
 * <p>
 * Renders a bunch of randomly generated boxes.
 */
public class UIRenderingSample implements Game {

    private static final String TITLE = "Shape Rendering Sample";
    private static final int SHAPE_COUNT = 5000;

    private UIRenderer renderer;
    private List<Vector2f> positions;
    private List<Vector2f> sizes;
    private List<Color> colors;

    @Override
    public void init() {
        Engine.setClearColor(Color.CORNFLOWER_BLUE);
        Engine.setBlendMode(BlendMode.TRANSPARENCY);
        this.renderer = new UIRenderer();
        this.positions = new ArrayList<>();
        this.sizes = new ArrayList<>();
        this.colors = new ArrayList<>();
        final Random random = new Random();
        for (int i = 0; i < SHAPE_COUNT; i++) {
            this.positions.add(new Vector2f(random.nextFloat(), random.nextFloat()));
            this.sizes.add(new Vector2f(random.nextFloat() * 0.3f, random.nextFloat() * 0.3f));
            this.colors.add(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat() * 0.4f + 0.6f));
        }
    }

    @Override
    public void destroy() {
        this.renderer.destroy();
    }

    @Override
    public void update(Time time) {
    }

    @Override
    public void render() {
        this.renderer.start();
        for (int i = 0; i < SHAPE_COUNT; i++) {
            final Vector2f position = this.positions.get(i);
            final Vector2f size = this.sizes.get(i);
            final Color color = this.colors.get(i);
            this.renderer.renderBox(position.x(), position.y(), size.x(), size.y(), color);
        }
        this.renderer.end();
    }

    public static void main(String[] args) {
        new Engine(new UIRenderingSample(), TITLE).start();
    }

}
