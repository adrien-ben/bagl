package com.adrien.games.bagl.utils;

import java.io.File;

import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPositionNormalTexture;
import com.adrien.games.bagl.rendering.VertexPositionTexture;

public final class MeshFactory {

	private MeshFactory() {
	}
	
	public static Mesh createRoom(float width , float height, float depth) {
		Texture texture = new Texture(new File(MeshFactory.class.getResource("/default.png").getFile()).getAbsolutePath());
		float xOffset = 0.5f / texture.getWidth();
		float yOffset = 0.5f / texture.getHeight();
		
		Vertex[] vertices = new Vertex[12];
		vertices[0] = new VertexPositionNormalTexture(new Vector3(-width/2,  0, depth/2), new Vector3(0, 1, 0), new Vector2(xOffset, yOffset));
		vertices[1] = new VertexPositionNormalTexture(new Vector3(width/2,  0, depth/2), new Vector3(0, 1, 0), new Vector2(10 - xOffset, yOffset));
		vertices[2] = new VertexPositionNormalTexture(new Vector3(-width/2,  0, -depth/2), new Vector3(0, 1, 0), new Vector2(xOffset, 10 - yOffset));
		vertices[3] = new VertexPositionNormalTexture(new Vector3(width/2,  0, -depth/2), new Vector3(0, 1, 0), new Vector2(10 - xOffset, 10 - yOffset));
		vertices[4] = new VertexPositionNormalTexture(new Vector3(width/2,  0, -depth/2), new Vector3(-1, 0, 0), new Vector2(xOffset, yOffset));
		vertices[5] = new VertexPositionNormalTexture(new Vector3(width/2,  0, depth/2), new Vector3(-1, 0, 0), new Vector2(10 - xOffset, yOffset));
		vertices[6] = new VertexPositionNormalTexture(new Vector3(width/2,  height, depth/2), new Vector3(-1, 0, 0), new Vector2(10 - xOffset, 5 - yOffset));
		vertices[7] = new VertexPositionNormalTexture(new Vector3(width/2,  height, -depth/2), new Vector3(-1, 0, 0), new Vector2(xOffset, 5 - yOffset));
		vertices[8] = new VertexPositionNormalTexture(new Vector3(-width/2,  0, -depth/2), new Vector3(0, 0, 1), new Vector2(xOffset, yOffset));
		vertices[9] = new VertexPositionNormalTexture(new Vector3(width/2,  0, -depth/2), new Vector3(0, 0, 1), new Vector2(10 - xOffset, yOffset));
		vertices[10] = new VertexPositionNormalTexture(new Vector3(width/2,  height, -depth/2), new Vector3(0, 0, 1), new Vector2(10 - xOffset, 5 - yOffset));
		vertices[11] = new VertexPositionNormalTexture(new Vector3(-width/2,  height, -depth/2), new Vector3(0, 0, 1), new Vector2(xOffset, 5 - yOffset));

		int[] indices = new int[]{0, 1, 2, 2, 1, 3, 4, 5, 6, 6, 7, 4, 8, 9, 10, 10, 11, 8};
		
		IndexBuffer indexBuffer = new IndexBuffer(indices);
		VertexBuffer vertexBuffer = new VertexBuffer(VertexPositionNormalTexture.DESCRIPTION, vertices);
		
		Material material = new Material(texture, 12, 1);
		return new Mesh(vertexBuffer, indexBuffer, material);
	}
	
	public static Mesh createPlane(float width, float depth) {
		Texture texture = new Texture(new File(MeshFactory.class.getResource("/default.png").getFile()).getAbsolutePath());
		float xOffset = 0.5f / texture.getWidth();
		float yOffset = 0.5f / texture.getHeight();
		
		Vertex[] vertices = new Vertex[4];
		vertices[0] = new VertexPositionTexture(new Vector3(-width/2,  0, depth/2), new Vector2(xOffset, yOffset));
		vertices[1] = new VertexPositionTexture(new Vector3(width/2,  0, depth/2), new Vector2(8 - xOffset, yOffset));
		vertices[2] = new VertexPositionTexture(new Vector3(-width/2,  0, -depth/2), new Vector2(xOffset, 8 - yOffset));
		vertices[3] = new VertexPositionTexture(new Vector3(width/2,  0, -depth/2), new Vector2(8 - xOffset, 8 - yOffset));
		
		int[] indices = new int[]{0, 1, 2, 2, 1, 3};
		
		IndexBuffer indexBuffer = new IndexBuffer(indices);
		VertexBuffer vertexBuffer = new VertexBuffer(VertexPositionTexture.DESCRIPTION, vertices);
		
		Material material = new Material(texture, 12, 1);
		return new Mesh(vertexBuffer, indexBuffer, material);
	}
	
	public static Mesh createBox(float width, float height, float depth) {
		Texture texture = new Texture(new File(MeshFactory.class.getResource("/default.png").getFile()).getAbsolutePath());
		float xOffset = 0.5f / texture.getWidth();
		float yOffset = 0.5f / texture.getHeight();
		
		float haftWidth = width/2;
		float haftHeight = height/2;
		float haftDepth = depth/2;
		
		Vertex[] vertices = new Vertex[] {
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  -haftHeight, haftDepth), new Vector3(0, 0, 1), new Vector2(xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  -haftHeight, haftDepth), new Vector3(0, 0, 1), new Vector2(1 - xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  haftHeight, haftDepth), new Vector3(0, 0, 1), new Vector2(xOffset, 1 - yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  haftHeight, haftDepth), new Vector3(0, 0, 1), new Vector2(1 - xOffset, 1 - yOffset)),
			
			new VertexPositionNormalTexture(new Vector3(haftWidth,  -haftHeight, haftDepth), new Vector3(1, 0, 0), new Vector2(xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  -haftHeight, -haftDepth), new Vector3(1, 0, 0), new Vector2(1 - xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  haftHeight, haftDepth), new Vector3(1, 0, 0), new Vector2(xOffset, 1 - yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  haftHeight, -haftDepth), new Vector3(1, 0, 0), new Vector2(1 - xOffset, 1 - yOffset)),
			
			new VertexPositionNormalTexture(new Vector3(haftWidth,  -haftHeight, -haftDepth), new Vector3(0, 0, -1), new Vector2(xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  -haftHeight, -haftDepth), new Vector3(0, 0, -1), new Vector2(1 - xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  haftHeight, -haftDepth), new Vector3(0, 0, -1), new Vector2(xOffset, 1 - yOffset)),
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  haftHeight, -haftDepth), new Vector3(0, 0, -1), new Vector2(1 - xOffset, 1 - yOffset)),
			
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  -haftHeight, -haftDepth), new Vector3(-1, 0, 0), new Vector2(xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  -haftHeight, haftDepth), new Vector3(-1, 0, 0), new Vector2(1 - xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  haftHeight, -haftDepth), new Vector3(-1, 0, 0), new Vector2(xOffset, 1 - yOffset)),
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  haftHeight, haftDepth), new Vector3(-1, 0, 0), new Vector2(1 - xOffset, 1 - yOffset)),
			
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  haftHeight, haftDepth), new Vector3(0, 1, 0), new Vector2(xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  haftHeight, haftDepth), new Vector3(0, 1, 0), new Vector2(1 - xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  haftHeight, -haftDepth), new Vector3(0, 1, 0), new Vector2(xOffset, 1 - yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  haftHeight, -haftDepth), new Vector3(0, 1, 0), new Vector2(1 - xOffset, 1 - yOffset)),
			
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  -haftHeight, -haftDepth), new Vector3(0, -1, 0), new Vector2(xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth,  -haftHeight, -haftDepth), new Vector3(0, -1, 0), new Vector2(1 - xOffset, yOffset)),
			new VertexPositionNormalTexture(new Vector3(-haftWidth,  -haftHeight, haftDepth), new Vector3(0, -1, 0), new Vector2(xOffset, 1 - yOffset)),
			new VertexPositionNormalTexture(new Vector3(haftWidth, -haftHeight, haftDepth), new Vector3(0, -1, 0), new Vector2(1 - xOffset, 1 - yOffset))
		};
	
		int[] indices = new int[]{
			0, 1, 2, 2, 1, 3,
			4, 5, 6, 6, 5, 7,
			8, 9, 10, 10, 9, 11,
			12, 13, 14, 14, 13, 15,
			16, 17, 18, 18, 17, 19,
			20, 21, 22, 22, 21, 23
		};
		
		IndexBuffer indexBuffer = new IndexBuffer(indices);
		VertexBuffer vertexBuffer = new VertexBuffer(VertexPositionNormalTexture.DESCRIPTION, vertices);
		
		Material material = new Material(texture, 12, 1);
		return new Mesh(vertexBuffer, indexBuffer, material);
	}
	
}
