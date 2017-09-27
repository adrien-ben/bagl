package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.shape.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * {@link ShapeRenderer} sample class.
 *
 * Renders a bunch of randomly generated shapes.
 */
public class ShapeRenderingSample implements Game {

    private static final String TITLE = "Shape Rendering Sample";
    private static final int SHAPE_COUNT = 1025;

    private ShapeRenderer renderer;
    private List<Vector2> positions;
    private List<Vector2> sizes;
    private List<Color> colors;

    @Override
    public void init() {
        Engine.setClearColor(Color.CORNFLOWER_BLUE);
        Engine.setBlendMode(BlendMode.TRANSPARENCY);
        this.renderer = new ShapeRenderer();
        this.positions = new ArrayList<>();
        this.sizes = new ArrayList<>();
        this.colors = new ArrayList<>();
        final Random random = new Random();
        for(int i = 0; i < SHAPE_COUNT; i++) {
            this.positions.add(new Vector2(random.nextFloat(), random.nextFloat()));
            this.sizes.add(new Vector2(random.nextFloat()*0.3f, random.nextFloat()*0.3f));
            this.colors.add(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat()*0.4f + 0.6f));
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
            final Vector2 position = this.positions.get(i);
            final Vector2 size = this.sizes.get(i);
            final Color color = this.colors.get(i);
            this.renderer.renderRectangle(position.getX(), position.getY(), size.getX(), size.getY(), color);
        }
        this.renderer.end();
    }

    public static void main(String[] args) {
        new Engine(new ShapeRenderingSample(), TITLE).start();
    }

}
