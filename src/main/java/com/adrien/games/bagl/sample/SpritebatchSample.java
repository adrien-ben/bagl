package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.utils.FileUtils;
import org.joml.Vector2f;

import java.util.Random;

public class SpritebatchSample {

    private static final class TestGame implements Game {

        private static final String TITLE = "Spritebatch";
        private static final int SPRITE_COUNT = 10000;

        private int width;
        private int height;

        private Texture texture;
        private Spritebatch spritebatch;

        private final Vector2f[] positions = new Vector2f[SPRITE_COUNT];
        private final int[] sizes = new int[SPRITE_COUNT];
        private final float[] rotations = new float[SPRITE_COUNT];

        @Override
        public void init() {
            this.width = Configuration.getInstance().getXResolution();
            this.height = Configuration.getInstance().getYResolution();

            this.spritebatch = new Spritebatch(1024, this.width, this.height);

            this.texture = Texture.fromFile(FileUtils.getResourceAbsolutePath("/default.png"), true, TextureParameters.builder());

            Random r = new Random();
            for (int i = 0; i < SPRITE_COUNT; i++) {
                this.positions[i] = new Vector2f(r.nextFloat() * this.width, r.nextFloat() * this.height);
                this.sizes[i] = r.nextInt(32) + 32;
                this.rotations[i] = r.nextFloat() * 360;
            }

            Engine.setBlendMode(BlendMode.TRANSPARENCY);
            Engine.setClearColor(Color.CORNFLOWER_BLUE);
        }

        @Override
        public void update(Time time) {
            for (int i = 0; i < SPRITE_COUNT; i++) {
                this.rotations[i] += time.getElapsedTime() * 50;
            }
        }

        @Override
        public void render() {
            this.spritebatch.start();
            for (int i = 0; i < SPRITE_COUNT; i++) {
                this.spritebatch.draw(this.texture, this.positions[i], this.sizes[i], this.sizes[i], this.rotations[i], Color.WHITE);
            }
            this.spritebatch.end();
        }

        @Override
        public void destroy() {
            this.texture.destroy();
        }

    }

    public static void main(String[] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }

}
