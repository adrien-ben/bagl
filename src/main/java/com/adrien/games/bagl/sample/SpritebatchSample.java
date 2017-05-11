package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.*;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.utils.FileUtils;

import java.io.File;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class SpritebatchSample {

    private static final class TestGame implements Game {

        private static final String TITLE = "Spritebatch";
        private static final int SPRITE_COUNT = 100000;
        private static final String HELLO_WORLD = "Hello World ! :)";

        private int width;
        private int height;

        private Texture texture;
        private Font font;
        private Spritebatch spritebatch;

        private float xTextPos;
        private final Vector2[] positions = new Vector2[SPRITE_COUNT];
        private final int[] sizes = new int[SPRITE_COUNT];
        private final float[] rotations = new float[SPRITE_COUNT];

        @Override
        public void init() {
            this.width = Configuration.getInstance().getXResolution();
            this.height = Configuration.getInstance().getYResolution();

            this.spritebatch = new Spritebatch(1024, this.width, this.height);

            this.texture = new Texture(new File(TestGame.class.getResource("/default.png").getFile()).getAbsolutePath(),
                    new TextureParameters());

            this.font = new Font(FileUtils.getResourceAbsolutePath("/fonts/default.ttf"), 40);
            this.xTextPos = this.width/2 - this.font.getTextWidth(HELLO_WORLD)/2;

            Random r = new Random();
            for(int i = 0; i < SPRITE_COUNT; i++) {
                this.positions[i] = new Vector2(r.nextFloat() * this.width, r.nextFloat() * this.height);
                this.sizes[i] = r.nextInt(32) + 32;
                this.rotations[i] = r.nextFloat() * 360;
            }

            glEnable(GL_CULL_FACE);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glClearColor(100f/255, 149f/255, 237f/255, 1);
        }

        @Override
        public void update(Time time) {
            for(int i = 0; i < SPRITE_COUNT; i++) {
                this.rotations[i] += time.getElapsedTime()*50;
            }
        }

        @Override
        public void render() {
            this.spritebatch.start();
            for(int i = 0; i < SPRITE_COUNT; i++) {
                this.spritebatch.draw(this.texture, this.positions[i], this.sizes[i], this.sizes[i], this.rotations[i], Color.WHITE);
            }
            this.spritebatch.drawText(HELLO_WORLD, font, new Vector2(this.xTextPos, this.height - 50), Color.RED);
            this.spritebatch.end();
        }

        @Override
        public void destroy() {
            this.texture.destroy();
            this.font.destroy();
        }

    }

    public static void main(String [] args) {
        new Engine(new TestGame(), TestGame.TITLE).start();
    }

}
