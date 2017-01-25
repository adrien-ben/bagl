package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.utils.MeshFactory;

public final class TestForwardRendering {
	
	private final static class TestGame implements Game	{
		
		public final static String TITLE = "Forward Rendering";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		//mesh
		private Mesh mesh;
		
		//camera & transform
		private Camera camera;
		private Matrix4 transform;
		
		//shaders
		private Shader ambientShader;
		private Shader directionalShader;
		private Shader pointShader;
		private Shader spotShader;
		
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
		public void init() {
			this.initMesh();
			this.initCamTrasform();
			this.initShaders();
			this.initLights();
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		}
		
		private void initMesh()	{
			this.mesh = MeshFactory.createRoom(20, 10, 20);
		}
		
		private void initCamTrasform() {			
			this.camera = new Camera(new Vector3(0.0f, 3.0f, 10.0f), new Vector3(0.0f, -3.0f, -10.0f), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 0.1f, 1000f);
			this.transform = new Matrix4();
		}
		
		private void initShaders() {
			//ambient light shader
			this.ambientShader = new Shader();
			this.ambientShader.addVertexShader("/ambient.vert");
			this.ambientShader.addFragmentShader("/ambient.frag");
			this.ambientShader.compile();
			
			//directional lights shader
			this.directionalShader = new Shader();
			this.directionalShader.addVertexShader("/model.vert");
			this.directionalShader.addFragmentShader("/directional.frag");
			this.directionalShader.compile();
			
			//point light shader
			this.pointShader = new Shader();
			this.pointShader.addVertexShader("/model.vert");
			this.pointShader.addFragmentShader("/point.frag");
			this.pointShader.compile();
			
			//spot light shader
			this.spotShader = new Shader();
			this.spotShader.addVertexShader("/model.vert");
			this.spotShader.addFragmentShader("/spot.frag");
			this.spotShader.compile();
		}
		
		private void initLights() {
			//ambient light
			this.ambientColor = new Vector3(1.0f, 1.0f, 1.0f);
			this.ambientIntensity = 0.1f;
			
			//directional light 1
			this.directionalColor = new Vector3(0.0f, 1.0f, 0.2f);
			this.directionalDirection = new Vector3(0.0f, -1.0f, 1.0f);
			this.directionnalIntensity = 0.1f;
			
			//point light 1
			this.pointColor = new Vector3(0.0f, 1.0f, 1.0f);
			this.pointPosition = new Vector3(0.0f, 1.5f, -8.0f);
			this.pointAttenuation = new Vector3(0.0f,  0.0f, 1.5f);
			this.pointIntensity = 0.8f;
			this.pointRange = 10.0f;
			
			//spot light 1
			this.spotColor = new Vector3(1.0f, 1.0f, 0.7f);
			this.spotPosition = new Vector3(4.0f, 0.5f, 3.0f);
			this.spotDirection = new Vector3(-3.0f, -1.0f, 5.0f);
			this.spotAttenuation = new Vector3(0.0f, 0.0f, 0.3f);
			this.spotIntensity = 0.6f;
			this.spotCutOff = (float)Math.cos(Math.toRadians(20.0f));			
		}

		@Override
		public void update(Time time) {
			if(this.pointPosition.getX() > 9.0f || this.pointPosition.getX() < -9.0f) {
				this.dir *= -1;
			}
			this.pointPosition.setX(this.pointPosition.getX() + this.dir*time.getElapsedTime()*2.0f);
		}

		@Override
		public void render() {
			
			this.mesh.getMaterial().getDiffuseTexture().bind();
			this.mesh.getVertices().bind();
			this.mesh.getIndices().bind();
			
			//render with ambient
			this.ambientShader.bind();
			this.ambientShader.setUniform("uMatrices.model", this.transform);
			this.ambientShader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			this.ambientShader.setUniform("uBaseLight.color", this.ambientColor);
			this.ambientShader.setUniform("uBaseLight.intensity", this.ambientIntensity);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthFunc(GL11.GL_EQUAL);
			
			//render with directional	
			this.directionalShader.bind();
			this.directionalShader.setUniform("uMatrices.model", this.transform);
			this.directionalShader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			this.directionalShader.setUniform("uEyePosition", this.camera.getPosition());
			this.directionalShader.setUniform("uLight.base.color", this.directionalColor);
			this.directionalShader.setUniform("uLight.direction", this.directionalDirection);
			this.directionalShader.setUniform("uLight.base.intensity", this.directionnalIntensity);
			this.directionalShader.setUniform("uMaterial.specularIntensity", this.mesh.getMaterial().getSpecularIntensity());
			this.directionalShader.setUniform("uMaterial.specularExponent", this.mesh.getMaterial().getSpecularExponent());
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			//render with point
			this.pointShader.bind();
			this.pointShader.setUniform("uMatrices.model", this.transform);
			this.pointShader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			this.pointShader.setUniform("uEyePosition", this.camera.getPosition());
			this.pointShader.setUniform("uLight.base.color", this.pointColor);
			this.pointShader.setUniform("uLight.position", this.pointPosition);
			this.pointShader.setUniform("uLight.base.intensity", this.pointIntensity);
			this.pointShader.setUniform("uLight.attenuation", this.pointAttenuation);
			this.pointShader.setUniform("uLight.range", this.pointRange);
			this.pointShader.setUniform("uMaterial.specularIntensity", this.mesh.getMaterial().getSpecularIntensity());
			this.pointShader.setUniform("uMaterial.specularExponent", this.mesh.getMaterial().getSpecularExponent());
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			//render with spot
			this.spotShader.bind();
			this.spotShader.setUniform("uMatrices.model", this.transform);
			this.spotShader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			this.spotShader.setUniform("uEyePosition", this.camera.getPosition());
			this.spotShader.setUniform("uLight.point.base.color", this.spotColor);
			this.spotShader.setUniform("uLight.point.base.intensity", this.spotIntensity);
			this.spotShader.setUniform("uLight.point.position", this.spotPosition);
			this.spotShader.setUniform("uLight.point.attenuation", this.spotAttenuation);
			this.spotShader.setUniform("uLight.point.range", 10.f);
			this.spotShader.setUniform("uLight.direction", this.spotDirection);
			this.spotShader.setUniform("uLight.cutOff", this.spotCutOff);
			this.spotShader.setUniform("uMaterial.specularIntensity", this.mesh.getMaterial().getSpecularIntensity());
			this.spotShader.setUniform("uMaterial.specularExponent", this.mesh.getMaterial().getSpecularExponent());
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDepthFunc(GL11.GL_LESS);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy() {
			this.mesh.destroy();
			this.ambientShader.destroy();
			this.directionalShader.destroy();
			this.pointShader.destroy();
			this.spotShader.destroy();
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
