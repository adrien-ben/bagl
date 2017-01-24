package com.adrien.games.bagl.rendering;

public class Mesh {
	
	private VertexBuffer vertices;
	private IndexBuffer indices;
	private Material material;
	
	public Mesh(VertexBuffer vertices, IndexBuffer indices, Material material) {
		this.vertices = vertices;
		this.indices = indices;
		this.material = material;
	}
	
	public void destroy() {
		vertices.destroy();
		indices.destroy();
		material.destroy();
	}

	public VertexBuffer getVertices() {
		return vertices;
	}

	public IndexBuffer getIndices() {
		return indices;
	}

	public Material getMaterial() {
		return material;
	}	
}
