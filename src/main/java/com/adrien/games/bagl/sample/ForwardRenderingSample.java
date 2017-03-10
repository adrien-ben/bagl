package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glPointSize;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector3;
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
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPositionColor;
import com.adrien.games.bagl.utils.MeshFactory;

public final class ForwardRenderingSample {
	
	private static final class TestGame implements Game	{
		
		private static final String TITLE = "Forward Rendering";
		private static final int WIDTH = 1920;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		//meshes
		private Mesh plane;
		private Matrix4 world;
		private Mesh cube;
		private Matrix4 cubeWorld;
		
		//camera & transform
		private Camera camera;
		private Matrix4 wvp;
		
		//shaders
		private Shader ambientShader;
		private Shader directionalShader;
		private Shader pointShader;
		private Shader spotShader;
		private Shader debugShader;
		
		private Light ambient;
		private List<DirectionalLight> directionals = new ArrayList<>();
		private List<PointLight> points = new ArrayList<>();
		private List<SpotLight> spots = new ArrayList<>();
		
		private VertexBuffer lightPositions;
		
		@Override
		public void init() {
			this.initMeshes();
			this.initCamTrasform();
			this.initShaders();
			this.initLights();
			this.initLightsPosition();
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			glPointSize(6);
		}
		
		private void initMeshes()	{
			this.plane = MeshFactory.fromResourceFile("/models/floor/floor.obj");
			this.world = new Matrix4();
			this.cube = MeshFactory.fromResourceFile("/models/sphere/sphere.obj");
			this.cubeWorld = Matrix4.createTranslation(new Vector3(0, 0.5f, 0));
		}
		
		private void initCamTrasform() {			
			this.camera = new Camera(new Vector3(0f, 2f, 6f), new Vector3(0f, -2f, -6f), Vector3.UP, 
					(float)Math.toRadians(60f), (float)WIDTH/(float)HEIGHT, 1, 1000);
			this.wvp = new Matrix4();
		}
		
		private void initShaders() {
			//ambient light shader
			this.ambientShader = new Shader();
			this.ambientShader.addVertexShader("/model.vert");
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
			
			//lights positions shader
			this.debugShader = new Shader();
			this.debugShader.addVertexShader("/debug.vert");
			this.debugShader.addFragmentShader("/debug.frag");
			this.debugShader.compile();
		}
		
		private void initLights() {
			this.ambient = new Light(0.1f);
			this.directionals.add(new DirectionalLight(0.2f, Color.WHITE, new Vector3(0.5f, -2, 4)));
			this.directionals.add(new DirectionalLight(0.2f, Color.TURQUOISE, new Vector3(0.5f, -3, -4)));
			this.points.add(new PointLight(1f, Color.GREEN, new Vector3(4f, 0.5f, 2f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.YELLOW, new Vector3(-4f, 0.2f, 2f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.BLUE, new Vector3(0f, 0.5f, 3f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.PURPLE, new Vector3(0f, 3f, 0f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(2f, Color.TURQUOISE, new Vector3(-1f, 0.1f, 1f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.CYAN, new Vector3(3f, 0.6f, -3f), 7f, Attenuation.CLOSE));
			this.spots.add(new SpotLight(10f, Color.RED, new Vector3(-2f, 0.5f, -3f), 7f, Attenuation.CLOSE, 
					new Vector3(0f, -1f, 0.8f), 20f, 5f));
			this.spots.add(new SpotLight(2f, Color.WHITE, new Vector3(2f, 2f, 2f), 7f, Attenuation.CLOSE, 
					new Vector3(0f, -1f, -0f), 10f, 5f));
			this.spots.add(new SpotLight(1f, Color.ORANGE, new Vector3(-0.5f, 0.5f, 0.5f), 7f, Attenuation.CLOSE, 
					new Vector3(2f, 0.7f, -1f), 20f, 5f));
		}
		
		private void initLightsPosition() {
			Vertex[] vertices = new Vertex[this.points.size() + this.spots.size()];
			for(int i = 0; i < this.points.size(); i++) {
				PointLight light = this.points.get(i);
				vertices[i] = new VertexPositionColor(light.getPosition(), light.getColor());
			}
			for(int i = 0; i < this.spots.size(); i++) {
				SpotLight light = this.spots.get(i);
				vertices[i + this.points.size()] = new VertexPositionColor(light.getPosition(), light.getColor());
			}
			this.lightPositions = new VertexBuffer(VertexPositionColor.DESCRIPTION, vertices);
		}

		@Override
		public void update(Time time) {
			Matrix4.mul(this.cubeWorld, Matrix4.createRotation(Vector3.UP, (float)Math.toRadians(10*time.getElapsedTime())), this.cubeWorld);
		}

		@Override
		public void render() {
			
			//render with ambient
			this.ambientShader.bind();
			this.ambientShader.setUniform("uBaseLight.color", this.ambient.getColor());
			this.ambientShader.setUniform("uBaseLight.intensity", this.ambient.getIntensity());
			
			this.drawMesh(this.plane, this.world, this.ambientShader, this.camera);
			this.drawMesh(this.cube, this.cubeWorld, this.ambientShader, this.camera);

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthFunc(GL11.GL_EQUAL);
			
			//render with directional	
			this.directionalShader.bind();
			this.directionalShader.setUniform("uEyePosition", this.camera.getPosition());
			
			for(DirectionalLight directional : this.directionals) {
				this.directionalShader.setUniform("uLight.base.color", directional.getColor());
				this.directionalShader.setUniform("uLight.direction", directional.getDirection());
				this.directionalShader.setUniform("uLight.base.intensity", directional.getIntensity());
				
				this.drawMesh(this.plane, this.world, this.directionalShader, this.camera);
				this.drawMesh(this.cube, this.cubeWorld, this.directionalShader, this.camera);
			}
			
			//render with point
			this.pointShader.bind();
			this.pointShader.setUniform("uMatrices.world", this.world);
			this.pointShader.setUniform("uMatrices.wvp", this.camera.getViewProj());
			this.pointShader.setUniform("uEyePosition", this.camera.getPosition());
			
			for(PointLight point : this.points) {
				this.pointShader.setUniform("uLight.base.color", point.getColor());
				this.pointShader.setUniform("uLight.position", point.getPosition());
				this.pointShader.setUniform("uLight.base.intensity", point.getIntensity());
				this.pointShader.setUniform("uLight.attenuation", new Vector3(
						point.getAttenuation().getConstant(), 
						point.getAttenuation().getLinear(), 
						point.getAttenuation().getQuadratic()));
				this.pointShader.setUniform("uLight.range", point.getRadius());
				
				this.drawMesh(this.plane, this.world, this.pointShader, this.camera);
				this.drawMesh(this.cube, this.cubeWorld, this.pointShader, this.camera);
			}
			
			//render with spot
			this.spotShader.bind();
			this.spotShader.setUniform("uMatrices.world", this.world);
			this.spotShader.setUniform("uMatrices.wvp", this.camera.getViewProj());
			this.spotShader.setUniform("uEyePosition", this.camera.getPosition());
			
			for(SpotLight spot : this.spots) {
				this.spotShader.setUniform("uLight.point.base.color", spot.getColor());
				this.spotShader.setUniform("uLight.point.base.intensity", spot.getIntensity());
				this.spotShader.setUniform("uLight.point.position", spot.getPosition());
				this.spotShader.setUniform("uLight.point.attenuation", new Vector3(
						spot.getAttenuation().getConstant(), 
						spot.getAttenuation().getLinear(), 
						spot.getAttenuation().getQuadratic()));
				this.spotShader.setUniform("uLight.point.range", spot.getRadius());
				this.spotShader.setUniform("uLight.direction", spot.getDirection());
				this.spotShader.setUniform("uLight.cutOff", spot.getCutOff());
				this.spotShader.setUniform("uLight.outerCutOff", spot.getOuterCutOff());
				
				this.drawMesh(this.plane, this.world, this.spotShader, this.camera);
				this.drawMesh(this.cube, this.cubeWorld, this.spotShader, this.camera);
			}
			
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDepthFunc(GL11.GL_LESS);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
			
			this.renderLightsPositions();
		}
		
		private void drawMesh(Mesh mesh, Matrix4 world, Shader shader, Camera camera) {
			Matrix4.mul(camera.getViewProj(), world, this.wvp);
			
			mesh.getVertices().bind();
			mesh.getIndices().bind();
			mesh.getMaterial().applyTo(shader);
			shader.setUniform("uMatrices.world", world);
			shader.setUniform("uMatrices.wvp", this.wvp);
			GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
		}
		
		public void renderLightsPositions() {
			this.lightPositions.bind();
			this.debugShader.bind();
			this.debugShader.setUniform("viewProj", this.camera.getViewProj());
			glDrawArrays(GL_POINTS, 0, this.points.size() + this.spots.size());
			Shader.unbind();
			VertexBuffer.unbind();
		}

		@Override
		public void destroy() {
			this.plane.destroy();
			this.ambientShader.destroy();
			this.directionalShader.destroy();
			this.pointShader.destroy();
			this.spotShader.destroy();
			this.lightPositions.destroy();
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
