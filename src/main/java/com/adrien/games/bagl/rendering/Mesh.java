package com.adrien.games.bagl.rendering;

import java.util.Objects;

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
		if(Objects.nonNull(vertices)) {			
			vertices.destroy();
		}
		if(Objects.nonNull(indices)) {			
			indices.destroy();
		}
		if(Objects.nonNull(material)) {
			material.destroy();
		}			
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
