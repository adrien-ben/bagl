package com.adrien.games.bagl.utils;

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
	
	public static Mesh createRoom(int width , int height, int depth) {
		Vertex[] vertices = new Vertex[12];
		vertices[0] = new VertexPositionNormalTexture(new Vector3(-width/2,  0, depth/2), new Vector3(0, 1, 0), new Vector2(0, 0));
		vertices[1] = new VertexPositionNormalTexture(new Vector3(width/2,  0, depth/2), new Vector3(0, 1, 0), new Vector2(10, 0));
		vertices[2] = new VertexPositionNormalTexture(new Vector3(-width/2,  0, -depth/2), new Vector3(0, 1, 0), new Vector2(0, 10));
		vertices[3] = new VertexPositionNormalTexture(new Vector3(width/2,  0, -depth/2), new Vector3(0, 1, 0), new Vector2(10, 10));
		vertices[4] = new VertexPositionNormalTexture(new Vector3(width/2,  0, -depth/2), new Vector3(-1, 0, 0), new Vector2(0, 0));
		vertices[5] = new VertexPositionNormalTexture(new Vector3(width/2,  0, depth/2), new Vector3(-1, 0, 0), new Vector2(10, 0));
		vertices[6] = new VertexPositionNormalTexture(new Vector3(width/2,  height, depth/2), new Vector3(-1, 0, 0), new Vector2(10, 5));
		vertices[7] = new VertexPositionNormalTexture(new Vector3(width/2,  height, -depth/2), new Vector3(-1, 0, 0), new Vector2(0, 5));
		vertices[8] = new VertexPositionNormalTexture(new Vector3(-width/2,  0, -depth/2), new Vector3(0, 0, 1), new Vector2(0, 0));
		vertices[9] = new VertexPositionNormalTexture(new Vector3(width/2,  0, -depth/2), new Vector3(0, 0, 1), new Vector2(10, 0));
		vertices[10] = new VertexPositionNormalTexture(new Vector3(width/2,  height, -depth/2), new Vector3(0, 0, 1), new Vector2(10, 5));
		vertices[11] = new VertexPositionNormalTexture(new Vector3(-width/2,  height, -depth/2), new Vector3(0, 0, 1), new Vector2(0, 5));

		int[] indices = new int[]{0, 1, 2, 2, 1, 3, 4, 5, 6, 6, 7, 4, 8, 9, 10, 10, 11, 8};
		
		IndexBuffer indexBuffer = new IndexBuffer(indices);
		VertexBuffer vertexBuffer = new VertexBuffer(VertexPositionNormalTexture.DESCRIPTION, vertices);
		
		Material material = new Material(new Texture("/default.png"), 12, 1);
		return new Mesh(vertexBuffer, indexBuffer, material);
	}
	
	public static Mesh createPlane(int width, int depth) {
		Vertex[] vertices = new Vertex[4];
		vertices[0] = new VertexPositionTexture(new Vector3(-width/2,  0, depth/2), new Vector2(0, 0));
		vertices[1] = new VertexPositionTexture(new Vector3(width/2,  0, depth/2), new Vector2(8, 0));
		vertices[2] = new VertexPositionTexture(new Vector3(-width/2,  0, -depth/2), new Vector2(0, 8));
		vertices[3] = new VertexPositionTexture(new Vector3(width/2,  0, -depth/2), new Vector2(8, 8));
		
		int[] indices = new int[]{0, 1, 2, 2, 1, 3};
		
		IndexBuffer indexBuffer = new IndexBuffer(indices);
		VertexBuffer vertexBuffer = new VertexBuffer(VertexPositionTexture.DESCRIPTION, vertices);
		
		Material material = new Material(new Texture("/default.png"), 12, 1);
		return new Mesh(vertexBuffer, indexBuffer, material);
	}
}
