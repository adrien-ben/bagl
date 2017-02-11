package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Quaternion;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Transform;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.utils.MeshFactory;

public class TestTransform {
	
	private final static class TestGame implements Game {

		public final static String TITLE = "Transform";
		public final static int WIDTH = 512;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private Mesh mesh;
		private Shader shader;
		
		private Camera camera;
		private Transform transform;
				
		@Override
		public void init() {
			this.mesh = MeshFactory.createPlane(20, 20);
			
			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/texture.frag");
			this.shader.compile();
			
			this.camera = new Camera(new Vector3(0, 5, 20), Vector3.FORWARD, Vector3.UP,
					(float)Math.toRadians(70f), (float)WIDTH / (float)HEIGHT, 0.1f, 1000f);
			
			this.transform = new Transform();
			
			Quaternion q = new Quaternion();
			Quaternion.mul(new Quaternion((float)Math.toRadians(5), Vector3.BACKWARD), new Quaternion((float)Math.toRadians(5), Vector3.UP), q);
			
			this.transform.getPosition().setX(0);
			this.transform.setRotation(q);
			this.transform.getScale().setXYZ(2, 2, 2);
			
			Transform t = new Transform();
			t.getPosition().setX(5);
			
			t.transform(transform);
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {			
			this.shader.bind();
			this.shader.setUniform("uMatrices.model", this.transform.getTransformMatrix());
			this.shader.setUniform("uMatrices.mvp", Matrix4.mul(this.camera.getViewProj(), this.transform.getTransformMatrix()));
			
			this.mesh.getMaterial().getDiffuseTexture().bind();
			this.mesh.getVertices().bind();
			this.mesh.getIndices().bind();
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
			this.mesh.destroy();
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}