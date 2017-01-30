package com.adrien.games.bagl.rendering;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera2D;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;

public class Spritebatch {

	private static final int MAX_SIZE = 512;
	private static final int VERTICES_PER_SPRITE = 4;
	private static final int INDICES_PER_SPRITE = 6;
	private static final String VERTEX_SHADER = "/sprite.vert";
	private static final String FRAGMENT_SHADER = "/sprite.frag";
	
	private final Camera2D camera;
	private final Shader shader;
	private final int size;
	private final VertexBuffer vertexBuffer;
	private final IndexBuffer indexBuffer;
	private final Vertex[] vertices;
	private int drawnSprites;
	private boolean started;
	private Texture currentTexture;
	
	public Spritebatch(int size, int width, int height) {
		this.camera = new Camera2D(new Vector2(width/2, height/2), width, height);
		this.shader = new Shader();
		this.shader.addVertexShader(VERTEX_SHADER);
		this.shader.addFragmentShader(FRAGMENT_SHADER);
		this.shader.compile();
		this.size = size < MAX_SIZE ? size : MAX_SIZE;
		this.vertexBuffer = new VertexBuffer(VertexPositionTexture.DESCRIPTION, this.size*VERTICES_PER_SPRITE);
		this.vertices = new VertexPositionTexture[this.size*VERTICES_PER_SPRITE];
		this.indexBuffer = this.initIndexBuffer(this.size);
		this.drawnSprites = 0;
		this.started = false;
	}
	
	private IndexBuffer initIndexBuffer(int size) {
		int[] indices = new int[size*INDICES_PER_SPRITE];
		for(int i = 0; i < size; i++) {
			int offset = i*INDICES_PER_SPRITE;
			int firstIndex = i*VERTICES_PER_SPRITE;
			indices[offset] = firstIndex;
			indices[offset + 1] = firstIndex + 1;
			indices[offset + 2] = firstIndex + 2;
			indices[offset + 3] = firstIndex + 2;
			indices[offset + 4] = firstIndex + 1;
			indices[offset + 5] = firstIndex + 3;
		}
		return new IndexBuffer(indices);
	}
	
	public void start() {
		if(started) {
			throw new IllegalStateException("You must call Spritebatch::end before calling Spritebatch::start again.");
		}
		this.drawnSprites = 0;
		this.started = true;
	}
	
	public void checkStarted() {
		if(!started) {
			throw new IllegalStateException("You must call Spritebatch::start before calling Spritebatch::end.");
		}
	}
	
	public void end() {
		this.checkStarted();
		this.renderBatch();
		this.started = false;
	}
	
	public void draw(Texture texture, Vector2 position) {
		this.draw(texture, position, texture.getWidth(), texture.getHeight(), 0);
	}	
	
	public void draw(Texture texture, Vector2 position, float width, float height) {
		this.draw(texture, position, width, height, 0);
	}
	
	public void draw(Texture texture, Vector2 position, float width, float height, float rotation) {
		this.checkStarted();
		if(this.drawnSprites >= this.size || (this.currentTexture != null && texture != this.currentTexture)) {
			renderBatch();
		}
		
		this.currentTexture = texture;
		float xTexelOffset = 0.5f/texture.getWidth();
		float yTexelOffset = 0.5f/texture.getHeight();
		
		float xCenter = position.getX() + width/2;
		float yCenter = position.getY() + height/2;
		
		Vector3 botLeftPos = rotation == 0 ? new Vector3(position.getX(), position.getY(), 0) 
				: rotate(position.getX(), position.getY(), xCenter, yCenter, rotation);
		Vector3 botRightPos = rotation == 0 ? new Vector3(position.getX() + width, position.getY(), 0) 
				: rotate(position.getX() + width, position.getY(), xCenter, yCenter, rotation);
		Vector3 topLeftPos = rotation == 0 ? new Vector3(position.getX(), position.getY() + height, 0) 
				: rotate(position.getX(), position.getY() + height, xCenter, yCenter, rotation);
		Vector3 topRightPos = rotation == 0 ? new Vector3(position.getX() + width, position.getY() + height, 0) 
				: rotate(position.getX() + width, position.getY() + height, xCenter, yCenter, rotation);

		int offset = this.drawnSprites*VERTICES_PER_SPRITE;
		
		this.vertices[offset] = new VertexPositionTexture(botLeftPos, new Vector2(xTexelOffset, yTexelOffset));
		this.vertices[offset + 1] = new VertexPositionTexture(botRightPos, new Vector2(1 - xTexelOffset, yTexelOffset));
		this.vertices[offset + 2] = new VertexPositionTexture(topLeftPos, new Vector2(xTexelOffset, 1 - yTexelOffset));
		this.vertices[offset + 3] = new VertexPositionTexture(topRightPos, new Vector2(1 - xTexelOffset, 1 - yTexelOffset));
		
		this.drawnSprites++;
	}
	
	private Vector3 rotate(float x, float y, float xCenter, float yCenter, float rotation) {
		float xOrigin = x - xCenter;
		float yOrigin = y - yCenter;
		
		double angleInRads = -(rotation*Math.PI/180f);
    	double cos = Math.cos(angleInRads);
    	double sin = Math.sin(angleInRads);
    	
    	double _x = xOrigin*cos - yOrigin*sin;
    	double _y = xOrigin*sin + yOrigin*cos;    	
    	return new Vector3((float)_x + xCenter, (float)_y + yCenter, 0);
	}
	
	private void renderBatch() {
		this.shader.bind();
		this.shader.setUniform("uCamera", this.camera.getOrthographic());
		this.currentTexture.bind();
		
		this.vertexBuffer.setData(this.vertices, this.drawnSprites*VERTICES_PER_SPRITE);
		
		this.vertexBuffer.bind();
		this.indexBuffer.bind();
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, this.drawnSprites*INDICES_PER_SPRITE, GL11.GL_UNSIGNED_INT, 0);
		
		this.drawnSprites = 0;
		
		IndexBuffer.unbind();
		VertexBuffer.unbind();
		Texture.unbind();
		Shader.unbind();
	}
	
}
