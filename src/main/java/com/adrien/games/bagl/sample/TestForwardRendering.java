package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.light.Attenuation;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.MeshFactory;

public final class TestForwardRendering {
	
	private static final class TestGame implements Game	{
		
		private static final String TITLE = "Forward Rendering";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
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
		
		private Light ambient;
		private DirectionalLight directional;
		private PointLight point;
		private SpotLight spot;
		
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
			this.ambient = new Light(0.1f);
			this.directional = new DirectionalLight(0.1f, new Color(0f, 1f, 0.2f), new Vector3(0.0f, -1.0f, 1.0f));
			this.point = new PointLight(0.8f, new Color(0.0f, 1.0f, 1.0f), new Vector3(0.0f, 1.5f, -8.0f), 10f, 
					new Attenuation(0.0f,  0.0f, 1.5f));
			this.spot = new SpotLight(0.6f, new Color(1.0f, 1.0f, 0.7f), new Vector3(4.0f, 0.5f, 3.0f), 10f,
					new Attenuation(0.0f, 0.0f, 0.3f), new Vector3(-3.0f, -1.0f, 5.0f), 20f, 0f);		
		}

		@Override
		public void update(Time time) {
			this.point.getPosition().setX((float)Math.sin(time.getTotalTime())*9);
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
			this.ambientShader.setUniform("uBaseLight.color", this.ambient.getColor());
			this.ambientShader.setUniform("uBaseLight.intensity", this.ambient.getIntensity());
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthFunc(GL11.GL_EQUAL);
			
			//render with directional	
			this.directionalShader.bind();
			this.directionalShader.setUniform("uMatrices.world", this.transform);
			this.directionalShader.setUniform("uMatrices.wvp", this.camera.getViewProj());
			this.directionalShader.setUniform("uEyePosition", this.camera.getPosition());
			this.directionalShader.setUniform("uLight.base.color", this.directional.getColor());
			this.directionalShader.setUniform("uLight.direction", this.directional.getDirection());
			this.directionalShader.setUniform("uLight.base.intensity", this.directional.getIntensity());
			this.directionalShader.setUniform("uMaterial.specularIntensity", this.mesh.getMaterial().getSpecularIntensity());
			this.directionalShader.setUniform("uMaterial.specularExponent", this.mesh.getMaterial().getSpecularExponent());
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			//render with point
			this.pointShader.bind();
			this.pointShader.setUniform("uMatrices.world", this.transform);
			this.pointShader.setUniform("uMatrices.wvp", this.camera.getViewProj());
			this.pointShader.setUniform("uEyePosition", this.camera.getPosition());
			this.pointShader.setUniform("uLight.base.color", this.point.getColor());
			this.pointShader.setUniform("uLight.position", this.point.getPosition());
			this.pointShader.setUniform("uLight.base.intensity", this.point.getIntensity());
			this.pointShader.setUniform("uLight.attenuation", new Vector3(
					this.point.getAttenuation().getConstant(), 
					this.point.getAttenuation().getLinear(), 
					this.point.getAttenuation().getQuadratic()));
			this.pointShader.setUniform("uLight.range", this.point.getRadius());
			this.pointShader.setUniform("uMaterial.specularIntensity", this.mesh.getMaterial().getSpecularIntensity());
			this.pointShader.setUniform("uMaterial.specularExponent", this.mesh.getMaterial().getSpecularExponent());
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			//render with spot
			this.spotShader.bind();
			this.spotShader.setUniform("uMatrices.world", this.transform);
			this.spotShader.setUniform("uMatrices.wvp", this.camera.getViewProj());
			this.spotShader.setUniform("uEyePosition", this.camera.getPosition());
			this.spotShader.setUniform("uLight.point.base.color", this.spot.getColor());
			this.spotShader.setUniform("uLight.point.base.intensity", this.spot.getIntensity());
			this.spotShader.setUniform("uLight.point.position", this.spot.getPosition());
			this.spotShader.setUniform("uLight.point.attenuation", new Vector3(
					this.spot.getAttenuation().getConstant(), 
					this.spot.getAttenuation().getLinear(), 
					this.spot.getAttenuation().getQuadratic()));
			this.spotShader.setUniform("uLight.point.range", this.spot.getRadius());
			this.spotShader.setUniform("uLight.direction", this.spot.getDirection());
			this.spotShader.setUniform("uLight.cutOff", this.spot.getCutOff());
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
