package com.adrien.games.bagl.sample;

import java.io.File;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.parser.model.ModelParser;
import com.adrien.games.bagl.parser.model.ObjParser;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.scene.SceneNode;
import com.adrien.games.bagl.rendering.texture.Texture;

public class ObjModelSample {

	private static final class TestGame implements Game {
				
		private static final String TITLE = "ObjModel";
		
		private int width;
		private int height;
		
		private SceneNode<Mesh> scene;
		
		private ModelParser parser = new ObjParser();
		private Matrix4 wvpBuff;
		private Shader shader;
		private Camera camera;
		
		private float lightIntensity;
		
		@Override
		public void init() {
			this.width = Configuration.getInstance().getXResolution();
			this.height = Configuration.getInstance().getYResolution();
			
			final Mesh mesh = this.parser.parse(new File(TestGame.class.getResource("/models/cube/cube.obj").getFile()).getAbsolutePath());
			this.scene = new SceneNode<>(mesh);
			this.scene.getLocalTransform()
				.setTranslation(new Vector3(2, 0, 0))
				.setRotation(Quaternion.fromAngleAndVector((float)Math.toRadians(45f), Vector3.UP))
				.setScale(new Vector3(2, 2, 2));
			
			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/ambient.frag");
			this.shader.compile();
			
			this.camera = new Camera(new Vector3(0, 2, 5), new Vector3(0, -2, -5), Vector3.UP, 
					(float)Math.toRadians(70f), (float)this.width/(float)this.height, 1f, 1000f);
			
			this.wvpBuff = Matrix4.createZero();
			
			this.lightIntensity = 1f;
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		@Override
		public void update(Time time) {		
			Matrix4.mul(this.camera.getViewProj(), this.scene.getTransform().getTransformMatrix(), this.wvpBuff);
		}

		@Override
		public void render() {
			this.shader.bind();
			this.shader.setUniform("uMatrices.world", this.scene.getTransform().getTransformMatrix());
			this.shader.setUniform("uMatrices.wvp", this.wvpBuff);
			this.shader.setUniform("uBaseLight.intensity", this.lightIntensity);
			this.shader.setUniform("uBaseLight.color", Color.WHITE);
			this.scene.get().getMaterial().applyTo(this.shader);
			
			this.scene.get().getVertices().bind();
			this.scene.get().getIndices().bind();
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.scene.get().getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
			this.scene.get().destroy();
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE).start();
	}
	
}
