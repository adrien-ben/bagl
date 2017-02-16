package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import com.adrien.games.bagl.core.Camera;
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
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPositionTexture;
import com.adrien.games.bagl.utils.MeshFactory;

public class DeferredRenderingSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Deferred Rendering";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private FrameBuffer gbuffer;
		
		private Mesh mesh;
		private Matrix4 world;
		private Matrix4 world2;
		private Matrix4 world3;
		private Matrix4 wvp;
		private float rotation;
		
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer;
		
		private Shader gbufferShader;
		private Shader deferredShader;
		private Camera camera;
		
		private Spritebatch spritebatch;
		
		@Override
		public void init() {

			this.gbuffer = new FrameBuffer(WIDTH, HEIGHT, 3);
			
			this.mesh = MeshFactory.createBox(1, 1, 1);
			this.world = new Matrix4();
			this.world2 = Matrix4.createTranslation(new Vector3(-1, 0, -2));
			this.world3 = Matrix4.createTranslation(new Vector3(1, 0, -3));
			this.wvp = new Matrix4();
			
			this.initQuad();

			this.gbufferShader = new Shader();
			this.gbufferShader.addVertexShader("/gbuffer.vert");
			this.gbufferShader.addFragmentShader("/gbuffer.frag");
			this.gbufferShader.compile();
			
			this.deferredShader = new Shader();
			this.deferredShader.addVertexShader("/deferred.vert");
			this.deferredShader.addFragmentShader("/deferred.frag");
			this.deferredShader.compile();

			this.camera = new Camera(new Vector3(0, 1, 2), new Vector3(0, -1, -2), Vector3.UP, 
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
			this.rotation += 1/(2*Math.PI)*2*time.getElapsedTime();
			this.world.setRotation(Vector3.UP, this.rotation);
		}

		@Override
		public void render() {
			this.renderGbuffer();			
			this.renderDeferred();
			
			this.spritebatch.start();
			this.spritebatch.draw(this.gbuffer.getColorTexture(0), Vector2.ZERO, WIDTH/3, HEIGHT/3);
			this.spritebatch.draw(this.gbuffer.getColorTexture(1), new Vector2(0, HEIGHT/3), WIDTH/3, HEIGHT/3);
			this.spritebatch.draw(this.gbuffer.getDepthTexture(), new Vector2(0, 2*HEIGHT/3), WIDTH/3, HEIGHT/3);
			this.spritebatch.draw(this.gbuffer.getColorTexture(2), new Vector2(2*WIDTH/3, 2*HEIGHT/3), 
					WIDTH/3, HEIGHT/3);
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
			
			Matrix4.mul(this.camera.getViewProj(), this.world2, this.wvp);
			this.gbufferShader.setUniform("uMatrices.world", this.world2);
			this.gbufferShader.setUniform("uMatrices.wvp", this.wvp);
			glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
			
			Matrix4.mul(this.camera.getViewProj(), this.world3, this.wvp);
			this.gbufferShader.setUniform("uMatrices.world", this.world3);
			this.gbufferShader.setUniform("uMatrices.wvp", this.wvp);
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
			this.deferredShader.setUniform("viewProj", this.camera.getViewProj());
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
