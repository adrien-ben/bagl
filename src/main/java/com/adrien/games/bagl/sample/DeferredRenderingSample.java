package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.light.Attenuation;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPositionTexture;
import com.adrien.games.bagl.utils.MeshFactory;

public class DeferredRenderingSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Deferred Rendering";
		private static final int WIDTH = 1024;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private FrameBuffer gbuffer;
		
		private Mesh mesh;
		private Matrix4 world;
		private Matrix4 wvp;
		
		private Light ambient;
		private DirectionalLight directional;
		private PointLight point;
		private SpotLight spot;
		
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer;
		
		private Shader gbufferShader;
		private Shader deferredShader;
		private Camera camera;
		
		private Spritebatch spritebatch;
		
		@Override
		public void init() {

			this.gbuffer = new FrameBuffer(WIDTH, HEIGHT, 2);
			
			this.mesh = MeshFactory.createPlane(10, 10);
			this.world = new Matrix4();
			this.wvp = new Matrix4();
			
			this.ambient = new Light(0.1f);
			this.directional = new DirectionalLight(0.1f, Color.WHITE, new Vector3(0.5f, -1, 4));
			this.point = new PointLight(1f, Color.GREEN, new Vector3(4f, 0.5f, 1f), 7f, new Attenuation(1, 0.7f, 1.8f));
			this.spot = new SpotLight(1f, Color.RED, new Vector3(0, 0.5f, 0f), 7f, new Attenuation(1, 0.7f, 1.8f), 
					new Vector3(0, -1, 0.8f), 20f, 5f);
			
			this.initQuad();

			this.gbufferShader = new Shader();
			this.gbufferShader.addVertexShader("/gbuffer.vert");
			this.gbufferShader.addFragmentShader("/gbuffer.frag");
			this.gbufferShader.compile();
			
			this.deferredShader = new Shader();
			this.deferredShader.addVertexShader("/deferred.vert");
			this.deferredShader.addFragmentShader("/deferred.frag");
			this.deferredShader.compile();

			this.camera = new Camera(new Vector3(0, 2f, 6f), new Vector3(0, -2f, -6f), Vector3.UP, 
					(float)Math.toRadians(60f), (float)WIDTH/(float)HEIGHT, 1, 1000);		
			
			this.spritebatch = new Spritebatch(1024, WIDTH, HEIGHT);
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
		}
		
		private void initQuad() {
			float uOffset = 1/WIDTH;
			float vOffset = 1/HEIGHT;
			
			Vertex[] vertices = new Vertex[4];
			vertices[0] = new VertexPositionTexture(new Vector3(-1, -1, 0), new Vector2(uOffset, vOffset));
			vertices[1] = new VertexPositionTexture(new Vector3(1, -1, 0), new Vector2(1 - uOffset, vOffset));
			vertices[2] = new VertexPositionTexture(new Vector3(-1, 1, 0), new Vector2(uOffset, 1 - vOffset));
			vertices[3] = new VertexPositionTexture(new Vector3(1, 1, 0), new Vector2(1 - uOffset, 1 - vOffset));

			int[] indices = new int[]{0, 1, 2, 2, 1, 3};
			
			this.indexBuffer = new IndexBuffer(indices);
			this.vertexBuffer = new VertexBuffer(VertexPositionTexture.DESCRIPTION, vertices);
		}
		
		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {
			this.renderGbuffer();			
			this.renderDeferred();
			
			this.spritebatch.start();
			this.spritebatch.draw(this.gbuffer.getColorTexture(0), Vector2.ZERO, WIDTH/3, HEIGHT/3);
			this.spritebatch.draw(this.gbuffer.getColorTexture(1), new Vector2(0, HEIGHT/3), WIDTH/3, HEIGHT/3);
			this.spritebatch.draw(this.gbuffer.getDepthTexture(), new Vector2(0, 2*HEIGHT/3), WIDTH/3, HEIGHT/3);
			this.spritebatch.end();
		}

		private void renderGbuffer() {
			
			Matrix4.mul(this.camera.getViewProj(), this.world, this.wvp);
			
			this.mesh.getMaterial().getDiffuseTexture().bind();
			this.mesh.getVertices().bind();
			this.mesh.getIndices().bind();
			this.gbufferShader.bind();
			this.gbufferShader.setUniform("uMatrices.world", this.world);
			this.gbufferShader.setUniform("uMatrices.wvp", this.wvp);
			this.gbuffer.bind();
			FrameBuffer.clear();
			
			glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
			
			FrameBuffer.unbind();
			Shader.unbind();
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
		}
		
		private void renderDeferred() {
			this.gbuffer.getColorTexture(0).bind(0);
			this.gbuffer.getColorTexture(1).bind(1);
			this.gbuffer.getDepthTexture().bind(2);
			this.vertexBuffer.bind();
			this.indexBuffer.bind();
			this.deferredShader.bind();
			this.deferredShader.setUniform("uCamera.vp", this.camera.getViewProj());
			this.deferredShader.setUniform("uCamera.position", this.camera.getPosition());
			this.deferredShader.setUniform("uAmbient.intensity", this.ambient.getIntensity());
			this.deferredShader.setUniform("uAmbient.color", this.ambient.getColor());
			this.deferredShader.setUniform("uDirectional.base.intensity", this.directional.getIntensity());
			this.deferredShader.setUniform("uDirectional.base.color", this.directional.getColor());
			this.deferredShader.setUniform("uDirectional.direction", this.directional.getDirection());
			this.deferredShader.setUniform("uPoints[0].base.intensity", this.point.getIntensity());
			this.deferredShader.setUniform("uPoints[0].base.color", this.point.getColor());
			this.deferredShader.setUniform("uPoints[0].position", this.point.getPosition());
			this.deferredShader.setUniform("uPoints[0].radius", this.point.getRadius());
			this.deferredShader.setUniform("uPoints[0].attenuation.constant", this.point.getAttenuation().getConstant());
			this.deferredShader.setUniform("uPoints[0].attenuation.linear", this.point.getAttenuation().getLinear());
			this.deferredShader.setUniform("uPoints[0].attenuation.quadratic", this.point.getAttenuation().getQuadratic());
			this.deferredShader.setUniform("uSpots[0].point.base.intensity", this.spot.getIntensity());
			this.deferredShader.setUniform("uSpots[0].point.base.color", this.spot.getColor());
			this.deferredShader.setUniform("uSpots[0].point.position", this.spot.getPosition());
			this.deferredShader.setUniform("uSpots[0].point.radius", this.spot.getRadius());
			this.deferredShader.setUniform("uSpots[0].point.attenuation.constant", this.spot.getAttenuation().getConstant());
			this.deferredShader.setUniform("uSpots[0].point.attenuation.linear", this.spot.getAttenuation().getLinear());
			this.deferredShader.setUniform("uSpots[0].point.attenuation.quadratic", this.spot.getAttenuation().getQuadratic());
			this.deferredShader.setUniform("uSpots[0].direction", this.spot.getDirection());
			this.deferredShader.setUniform("uSpots[0].cutOff", this.spot.getCutOff());
			this.deferredShader.setUniform("uSpots[0].outerCutOff", this.spot.getOuterCutOff());
			this.deferredShader.setUniform("colors", 0);
			this.deferredShader.setUniform("normals", 1);
			this.deferredShader.setUniform("depth", 2);
			
			glDrawElements(GL_TRIANGLES, this.indexBuffer.getSize(), GL_UNSIGNED_INT, 0);
			
			Shader.unbind();
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind(0);
			Texture.unbind(1);
			Texture.unbind(2);
		}
		
		@Override
		public void destroy() {
			this.gbufferShader.destroy();
			this.deferredShader.destroy();
			this.mesh.destroy();
			this.gbuffer.destroy();
			this.indexBuffer.destroy();
			this.vertexBuffer.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
	
}
