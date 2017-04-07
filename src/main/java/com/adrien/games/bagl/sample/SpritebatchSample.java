package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.utils.FileUtils;

public class SpritebatchSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Spritebatch";
		private static final int SPRITE_COUNT = 10000;

		private int width;
		private int height;
		
		private Texture texture;
		private Font font;
		private Spritebatch spritebatch;
		
		private Vector2[] positions = new Vector2[SPRITE_COUNT];
		private int[] sizes = new int[SPRITE_COUNT];
		private float[] rotations = new float[SPRITE_COUNT];
				
		@Override
		public void init() {
			this.width = Configuration.getInstance().getXResolution();
			this.height = Configuration.getInstance().getYResolution();
			
			this.spritebatch = new Spritebatch(512, this.width, this.height);
			
			this.texture = new Texture(new File(TestGame.class.getResource("/default.png").getFile()).getAbsolutePath(),
					new TextureParameters());
			
			this.font = new Font(FileUtils.getResourceAbsolutePath("/fonts/default.ttf"), 40);
			
			Random r = new Random();
			for(int i = 0; i < SPRITE_COUNT; i++) {
				positions[i] = new Vector2(r.nextFloat() * this.width, r.nextFloat() * this.height);
				sizes[i] = r.nextInt(32) + 32;
				rotations[i] = r.nextFloat() * 360;
			}
			
			glEnable(GL11.GL_CULL_FACE);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glClearColor(100f/255, 149f/255, 237f/255, 1);
		}

		@Override
		public void update(Time time) {
			for(int i = 0; i < SPRITE_COUNT; i++) {
				rotations[i] += time.getElapsedTime()*50;
			}
		}

		@Override
		public void render() {
			this.spritebatch.start();
			for(int i = 0; i < SPRITE_COUNT; i++) {
				this.spritebatch.draw(this.texture, positions[i], sizes[i], sizes[i], rotations[i], Color.WHITE);
			}
			this.spritebatch.drawText("Hello World ! :)", font, new Vector2(this.width/2, 20), Color.RED);
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
