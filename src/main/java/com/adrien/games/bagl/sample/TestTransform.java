package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Quaternion;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Transform;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPositionTexture;

public class TestTransform
{
	private final static class TestGame implements Game
	{

		public final static String TITLE = "Transform";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private VertexBuffer vb;
		private IndexBuffer ib;
		private Shader shader;
		private Texture texture;
		
		private Camera camera;
		private Transform transform;
				
		@Override
		public void init()
		{
			Vertex[] vertices = new Vertex[4];
			vertices[0] = new VertexPositionTexture(new Vector3(-10,  0, 10), new Vector2(0, 0));
			vertices[1] = new VertexPositionTexture(new Vector3(10,  0, 10), new Vector2(8, 0));
			vertices[2] = new VertexPositionTexture(new Vector3(-10,  0, -10), new Vector2(0, 8));
			vertices[3] = new VertexPositionTexture(new Vector3(10,  0, -10), new Vector2(8, 8));
			int[] indices = new int[]{0, 1, 2, 2, 1, 3 };
			
			ib = new IndexBuffer(indices);
			vb = new VertexBuffer(VertexPositionTexture.DESCRIPTION, vertices);
			
			shader = new Shader();
			shader.addVertexShader("/model.vert");
			shader.addFragmentShader("/texture.frag");
			shader.compile();
			
			texture = new Texture("/default.png");
			
			camera = new Camera(new Vector3(0, 5, 20), Vector3.FORWARD, Vector3.UP,
					(float)Math.toRadians(70f), (float)WIDTH / (float)HEIGHT, 0.1f, 1000f);
			
			transform = new Transform();
			
			Quaternion q = new Quaternion();
			Quaternion.mul(new Quaternion((float)Math.toRadians(5), Vector3.BACKWARD), new Quaternion((float)Math.toRadians(5), Vector3.UP), q);
			
			transform.getPosition().setX(0);
			transform.setRotation(q);
			transform.getScale().setXYZ(2, 2, 2);
			
			Transform t = new Transform();
			t.getPosition().setX(5);
			
			t.transform(transform);
			
			System.out.println(t.getPosition());
			System.out.println(t.getScale());
			
		}

		@Override
		public void update(Time time)
		{
			
		}

		@Override
		public void render()
		{			
			shader.bind();
			shader.setUniform("uMatrices.model", transform.getTransformMatrix());
			shader.setUniform("uMatrices.mvp", Matrix4.mul(camera.getViewProj(), transform.getTransformMatrix()));
			
			texture.bind();
			
			vb.bind();
			ib.bind();
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, ib.getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy()
		{
			shader.destroy();
			ib.destroy();
			vb.destroy();
			texture.destroy();
		}

	}
	
	public static void main(String [] args)
	{
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
