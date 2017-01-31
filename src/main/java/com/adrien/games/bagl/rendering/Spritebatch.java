package com.adrien.games.bagl.rendering;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera2D;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;

/**
 * Class allowing to render sprites in batch.
 * <p>The purpose of this class is to limit the number of draw calls made when rendering sprites. A batch is rendered when :
 * <ul>
 * <li>The Spritebatch::end is called (generally before the end of the frame)
 * <li>The batch max size is reached.
 * <li>The current texture changes. (it means spritebatch works better if sprites are grouped
 *  by texture or with texture atlases) 
 * @author Adrien
 *
 */
public class Spritebatch {

	private static final int MAX_SIZE = 1024;
	private static final int VERTICES_PER_SPRITE = 4;
	private static final int INDICES_PER_SPRITE = 6;
	private static final String VERTEX_SHADER = "/sprite.vert";
	private static final String FRAGMENT_SHADER = "/sprite.frag";
	
	private final Camera2D camera;
	private final Shader shader;
	private final int size;
	private final VertexBuffer vertexBuffer;
	private final IndexBuffer indexBuffer;
	private final VertexPositionColorTexture[] vertices;
	private int drawnSprites;
	private boolean started;
	private Texture currentTexture;
	
	/**
	 * Instantiates and initializes the spritebatch.
	 * <p>Default camera and shader are created. 
	 * @param size The size of the spritebatch. Will be overridden if over {@value #MAX_SIZE}.
	 * @param width The width of the viewport.
	 * @param height The height of the viewport.
	 */
	public Spritebatch(int size, int width, int height) {
		this.camera = new Camera2D(new Vector2(width/2, height/2), width, height);
		this.shader = new Shader();
		this.shader.addVertexShader(VERTEX_SHADER);
		this.shader.addFragmentShader(FRAGMENT_SHADER);
		this.shader.compile();
		this.size = size < MAX_SIZE ? size : MAX_SIZE;
		this.vertexBuffer = new VertexBuffer(VertexPositionColorTexture.DESCRIPTION, this.size*VERTICES_PER_SPRITE);
		this.vertices = this.initVertices(this.size);
		this.indexBuffer = this.initIndexBuffer(this.size);
		this.drawnSprites = 0;
		this.started = false;
	}
	
	/**
	 * Initializes the index buffer.
	 * @param size The size of the spritebatch.
	 * @return The initialized {@link IndexBuffer}.
	 */
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
	
	/**
	 * Initializes the vertex pool.
	 * @param size The size of the spritebatch.
	 * @return An initialized array of {@link VertexPositionTexture}.
	 */
	private VertexPositionColorTexture[] initVertices(int size) {
		VertexPositionColorTexture[] vertices = new VertexPositionColorTexture[size*VERTICES_PER_SPRITE];
		for(int i = 0; i < vertices.length; i++) {
			vertices[i] = new VertexPositionColorTexture(new Vector3(), new Color(1, 1, 1, 1), new Vector2());
		}
		return vertices;
	}
	
	/**
	 * Starts the spritebatching.
	 * <p>This method re-initializes the state of the spritebatch. 
	 * Can only be called once before Spritebatch::end is called.
	 */
	public void start() {
		if(started) {
			throw new IllegalStateException("You must call Spritebatch::end before calling Spritebatch::start again.");
		}
		this.drawnSprites = 0;
		this.started = true;
	}
	
	/**
	 * Checks that the spritebatch is started before it is used.
	 */
	public void checkStarted() {
		if(!started) {
			throw new IllegalStateException("You must call Spritebatch::start before calling Spritebatch::end or Spritebatch::draw.");
		}
	}
	
	/**
	 * Ends the spritebatching.
	 * <p>Spritebatch::start must have been called before this one is called. 
	 * When called this methods renders the current batch. 
	 */
	public void end() {
		this.checkStarted();
		this.renderBatch();
		this.started = false;
	}
	
	/**
	 * Renders a sprite at a given position.
	 * @param texture The texture to render.
	 * @param position The position where to render the sprite.
	 */
	public void draw(Texture texture, Vector2 position) {
		this.draw(texture, position, texture.getWidth(), texture.getHeight(), 0, Color.WHITE);
	}	
	
	/**
	 * Renders a sprite at a given position and with a given size.
	 * @param texture The texture to render.
	 * @param position The position where to render the sprite.
	 * @param width The width of the sprite to render.
	 * @param height The height of the sprite to render.
	 */
	public void draw(Texture texture, Vector2 position, float width, float height) {
		this.draw(texture, position, width, height, 0, Color.WHITE);
	}
	
	/**
	 * Renders a sprite at a given position, with a given size and a given rotation.
	 * @param texture The texture to render.
	 * @param position The position where to render the sprite.
	 * @param width The width of the sprite to render.
	 * @param height The height of the sprite to render.
	 * @param rotation The rotation of the sprite.
	 */
	public void draw(Texture texture, Vector2 position, float width, float height, float rotation) {
		this.draw(texture, position, width, height, rotation, Color.WHITE);
	}
	
	/**
	 * Renders a sprite at a given position, with a given size and a given rotation.
	 * @param texture The texture to render.
	 * @param position The position where to render the sprite.
	 * @param width The width of the sprite to render.
	 * @param height The height of the sprite to render.
	 * @param rotation The rotation of the sprite.
	 * @param color The tint of the sprite.
	 */
	public void draw(Texture texture, Vector2 position, float width, float height, float rotation, Color color) {
		this.checkStarted();
		if(this.drawnSprites >= this.size || (this.currentTexture != null && texture != this.currentTexture)) {
			renderBatch();
		}
		
		this.currentTexture = texture;
		float xTexelOffset = 0.5f/texture.getWidth();
		float yTexelOffset = 0.5f/texture.getHeight();
		
		float x = position.getX();
		float y = position.getY();
		
		float xCenter = position.getX() + width/2;
		float yCenter = position.getY() + height/2;
		
		int offset = this.drawnSprites*VERTICES_PER_SPRITE;
		
		this.computeVertex(offset, x, y, xTexelOffset, yTexelOffset, rotation, xCenter, yCenter, color);
		this.computeVertex(offset + 1, x + width, y, 1 - xTexelOffset, yTexelOffset, rotation, xCenter, yCenter, color);
		this.computeVertex(offset + 2, x, y + height, xTexelOffset, 1 - yTexelOffset, rotation, xCenter, yCenter, color);
		this.computeVertex(offset + 3, x + width, y + height, 1 - xTexelOffset, 1 - yTexelOffset, rotation, xCenter, yCenter, color);
		
		this.drawnSprites++;
	}
	
	/**
	 * Compute the final position of a vertex.
	 * @param index The index of the vertex.
	 * @param x The initial x position of the vertex.
	 * @param y The initial y position of the vertex.
	 * @param xCoord The x texture coordinate.
	 * @param yCoord The y texture coordinate.
	 * @param rotation The rotation of the vertex.
	 * @param xCenter The x component of the rotation center.
	 * @param yCenter The y component of the rotation center.
	 */
	private void computeVertex(int index, float x, float y, float xCoord, float yCoord, 
			float rotation, float xCenter, float yCenter, Color color) {
		if(rotation != 0) {
			float xOrigin = x - xCenter;
			float yOrigin = y - yCenter;
			
			double angleInRads = -(rotation*Math.PI/180f);
	    	double cos = Math.cos(angleInRads);
	    	double sin = Math.sin(angleInRads);
	    	
	    	double _x = xOrigin*cos - yOrigin*sin;
	    	double _y = xOrigin*sin + yOrigin*cos;
	    	
	    	x = (float)_x + xCenter;
	    	y = (float)_y + yCenter;
		}
		
		this.vertices[index].getPosition().setX(x);
		this.vertices[index].getPosition().setY(y);
		this.vertices[index].getColor().setRed(color.getRed());
		this.vertices[index].getColor().setGreen(color.getGreen());
		this.vertices[index].getColor().setBlue(color.getBlue());
		this.vertices[index].getCoords().setX(xCoord);
		this.vertices[index].getCoords().setY(yCoord);
	}
	
	/**
	 * Renders the current batch.
	 * <ul>
	 * <li>Binds the shader and texture.
	 * <li>Updates the vertex buffer data.
	 * <li>Binds the vertex and index buffers.
	 * <li>Performs the draw call.
	 * <li>Unbinds everything.
	 */
	private void renderBatch() {
		if(this.drawnSprites > 0) {
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
	
}
