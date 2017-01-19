package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPositionNormalTexture;

public final class TestForwardRendering
{
	private final static class TestGame implements Game
	{
		public final static String TITLE = "Forward Rendering";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		//mesh
		private VertexBuffer vb;
		private IndexBuffer ib;
		
		//material
		private Texture texture;
		private float specExponent;
		private float specIntensity;
		
		//camera & transform
		private Vector3 eyePosition;
		private Matrix4 camera;
		private Matrix4 transform;
		
		//shaders
		Shader ambientShader;
		Shader directionalShader;
		Shader pointShader;
		Shader spotShader;
		
		//ambient light
		private Vector3 ambientColor;
		private float ambientIntensity;
		
		//directional light 1
		private Vector3 directionalColor;
		private Vector3 directionalDirection;
		private float directionnalIntensity;
		
		//point light 1
		private Vector3 pointColor;
		private Vector3 pointPosition;
		private Vector3 pointAttenuation;
		private float pointIntensity;
		private float pointRange;
		
		//spot light 1
		private Vector3 spotColor;
		private Vector3 spotPosition;
		private Vector3 spotDirection;
		private Vector3 spotAttenuation;
		private float spotIntensity;
		private float spotCutOff;
		
		//things
		int dir = 1;		
		
		@Override
		public void init()
		{
			initMesh();
			initMaterial();
			initCamTrasform();
			initShaders();
			initLights();
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		}
		
		private void initMesh()
		{
			Vertex[] vertices = new Vertex[12];
			vertices[0] = new VertexPositionNormalTexture(new Vector3(-10,  0, 10), new Vector3(0, 1, 0), new Vector2(0, 0));
			vertices[1] = new VertexPositionNormalTexture(new Vector3(10,  0, 10), new Vector3(0, 1, 0), new Vector2(10, 0));
			vertices[2] = new VertexPositionNormalTexture(new Vector3(-10,  0, -10), new Vector3(0, 1, 0), new Vector2(0, 10));
			vertices[3] = new VertexPositionNormalTexture(new Vector3(10,  0, -10), new Vector3(0, 1, 0), new Vector2(10, 10));
			
			vertices[4] = new VertexPositionNormalTexture(new Vector3(10,  0, -10), new Vector3(-1, 0, 0), new Vector2(0, 0));
			vertices[5] = new VertexPositionNormalTexture(new Vector3(10,  0, 10), new Vector3(-1, 0, 0), new Vector2(10, 0));
			vertices[6] = new VertexPositionNormalTexture(new Vector3(10,  10, 10), new Vector3(-1, 0, 0), new Vector2(10, 5));
			vertices[7] = new VertexPositionNormalTexture(new Vector3(10,  10, -10), new Vector3(-1, 0, 0), new Vector2(0, 5));
			
			vertices[8] = new VertexPositionNormalTexture(new Vector3(-10,  0, -10), new Vector3(0, 0, 1), new Vector2(0, 0));
			vertices[9] = new VertexPositionNormalTexture(new Vector3(10,  0, -10), new Vector3(0, 0, 1), new Vector2(10, 0));
			vertices[10] = new VertexPositionNormalTexture(new Vector3(10,  10, -10), new Vector3(0, 0, 1), new Vector2(10, 5));
			vertices[11] = new VertexPositionNormalTexture(new Vector3(-10,  10, -10), new Vector3(0, 0, 1), new Vector2(0, 5));

			int[] indices = new int[]{
					0, 1, 2, 2, 1, 3,
					4, 5, 6, 6, 7, 4,
					8, 9, 10, 10, 11, 8,
					};
			
			ib = new IndexBuffer(indices);
			vb = new VertexBuffer(VertexPositionNormalTexture.DESCRIPTION, vertices);
		}
		
		private void initMaterial()
		{
			texture = new Texture("/default.png");
			specExponent = 12.0f;
			specIntensity = 1.0f;
		}
		
		private void initCamTrasform()
		{
			eyePosition = new Vector3(0.0f, 3.0f, 10.0f);
			
			Matrix4 proj = new Matrix4();
			proj.setPerspective((float)Math.toRadians(60f), (float)WIDTH / (float)HEIGHT, 0.1f, 1000f);
			
			Matrix4 view = new Matrix4();
			view.setLookAt(eyePosition, Vector3.ZERO, Vector3.UP);
			
			camera = Matrix4.mul(proj, view);
			
			transform = new Matrix4();
		}
		
		private void initShaders()
		{
			//ambient light shader
			ambientShader = new Shader();
			ambientShader.addVertexShader("/ambient.vert");
			ambientShader.addFragmentShader("/ambient.frag");
			ambientShader.compile();
			
			//directional lights shader
			directionalShader = new Shader();
			directionalShader.addVertexShader("/model.vert");
			directionalShader.addFragmentShader("/directional.frag");
			directionalShader.compile();
			
			//point light shader
			pointShader = new Shader();
			pointShader.addVertexShader("/model.vert");
			pointShader.addFragmentShader("/point.frag");
			pointShader.compile();
			
			//spot light shader
			spotShader = new Shader();
			spotShader.addVertexShader("/model.vert");
			spotShader.addFragmentShader("/spot.frag");
			spotShader.compile();
		}
		
		private void initLights()
		{
			//ambient light
			ambientColor = new Vector3(1.0f, 1.0f, 1.0f);
			ambientIntensity = 0.1f;
			
			//directional light 1
			directionalColor = new Vector3(0.0f, 1.0f, 0.2f);
			directionalDirection = new Vector3(0.0f, -1.0f, 1.0f);
			directionnalIntensity = 0.1f;
			
			//point light 1
			pointColor = new Vector3(0.0f, 1.0f, 1.0f);
			pointPosition = new Vector3(0.0f, 1.5f, -8.0f);
			pointAttenuation = new Vector3(0.0f,  0.0f, 1.5f);
			pointIntensity = 0.8f;
			pointRange = 10.0f;
			
			//spot light 1
			spotColor = new Vector3(1.0f, 1.0f, 0.7f);
			spotPosition = new Vector3(4.0f, 0.5f, 3.0f);
			spotDirection = new Vector3(-3.0f, -1.0f, 5.0f);
			spotAttenuation = new Vector3(0.0f, 0.0f, 0.3f);
			spotIntensity = 0.6f;
			spotCutOff = (float)Math.cos(Math.toRadians(20.0f));
						
		}

		@Override
		public void update(Time time)
		{
			if(pointPosition.getX() > 9.0f || pointPosition.getX() < -9.0f)
			{
				dir *= -1;
			}
			
			pointPosition.setX(pointPosition.getX() + dir*time.getElapsedTime()*2.0f);
		}

		@Override
		public void render()
		{			
			texture.bind();
			vb.bind();
			ib.bind();
			
			//render with ambient
			ambientShader.bind();
			ambientShader.setUniform("uMatrices.model", transform);
			ambientShader.setUniform("uMatrices.mvp", camera);
			ambientShader.setUniform("uBaseLight.color", ambientColor);
			ambientShader.setUniform("uBaseLight.intensity", ambientIntensity);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, ib.getSize(), GL11.GL_UNSIGNED_INT, 0);

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthFunc(GL11.GL_EQUAL);
			
			//render with directional	
			directionalShader.bind();
			directionalShader.setUniform("uMatrices.model", transform);
			directionalShader.setUniform("uMatrices.mvp", camera);
			directionalShader.setUniform("uEyePosition", eyePosition);
			directionalShader.setUniform("uLight.base.color", directionalColor);
			directionalShader.setUniform("uLight.direction", directionalDirection);
			directionalShader.setUniform("uLight.base.intensity", directionnalIntensity);
			directionalShader.setUniform("uMaterial.specularIntensity", specIntensity);
			directionalShader.setUniform("uMaterial.specularExponent", specExponent);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, ib.getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			//render with point
			pointShader.bind();
			pointShader.setUniform("uMatrices.model", transform);
			pointShader.setUniform("uMatrices.mvp", camera);
			pointShader.setUniform("uEyePosition", eyePosition);
			pointShader.setUniform("uLight.base.color", pointColor);
			pointShader.setUniform("uLight.position", pointPosition);
			pointShader.setUniform("uLight.base.intensity", pointIntensity);
			pointShader.setUniform("uLight.attenuation", pointAttenuation);
			pointShader.setUniform("uLight.range", pointRange);
			pointShader.setUniform("uMaterial.specularIntensity", specIntensity);
			pointShader.setUniform("uMaterial.specularExponent", specExponent);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, ib.getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			//render with spot
			spotShader.bind();
			spotShader.setUniform("uMatrices.model", transform);
			spotShader.setUniform("uMatrices.mvp", camera);
			spotShader.setUniform("uEyePosition", eyePosition);
			spotShader.setUniform("uLight.point.base.color", spotColor);
			spotShader.setUniform("uLight.point.base.intensity", spotIntensity);
			spotShader.setUniform("uLight.point.position", spotPosition);
			spotShader.setUniform("uLight.point.attenuation", spotAttenuation);
			spotShader.setUniform("uLight.point.range", 10.f);
			spotShader.setUniform("uLight.direction", spotDirection);
			spotShader.setUniform("uLight.cutOff", spotCutOff);
			spotShader.setUniform("uMaterial.specularIntensity", specIntensity);
			spotShader.setUniform("uMaterial.specularExponent", specExponent);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, ib.getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDepthFunc(GL11.GL_LESS);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy()
		{
			ib.destroy();
			vb.destroy();
			
			texture.destroy();
			
			ambientShader.destroy();
			directionalShader.destroy();
			pointShader.destroy();
			spotShader.destroy();
		}

	}
	
	public static void main(String [] args)
	{
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
