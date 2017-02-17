package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.texture.Cubemap;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPosition;

public class Skybox {

	private final VertexBuffer vertexBuffer;
	private final IndexBuffer indexBuffer;
	private final Cubemap cubemap;

	/**
	 * Creates a sky box from the different faces file path. 
	 * @param left The path to the file containing the left face.
	 * @param right The path to the file containing the right face.
	 * @param bottom The path to the file containing the bottom face.
	 * @param top The path to the file containing the top face.
	 * @param back The path to the file containing the back face.
	 * @param front The path to the file containing the front face.
	 */
	public Skybox(String left, String right, String bottom, String top, String back, String front)  {
		this.vertexBuffer = initVertices();
		this.indexBuffer = initIndices();
		this.cubemap = new Cubemap(left, right, bottom, top, back, front);
	}

	private static IndexBuffer initIndices() {
		int[] indices = new int[] {
				1, 0, 3, 3, 0, 2,
				5, 1, 7, 7, 1, 3,
				4, 5, 6, 6, 5, 7,
				0, 4, 2, 2, 4, 6,
				6, 7, 2, 2, 7, 3,
				0, 1, 4, 4, 1, 5
			};
		return new IndexBuffer(indices);
	}

	private static VertexBuffer initVertices() {
		Vertex[] vertices = new VertexPosition[] {
				new VertexPosition(new Vector3(-2f, -2f, 2f)),
				new VertexPosition(new Vector3(2f, -2f, 2f)),
				new VertexPosition(new Vector3(-2f, 2f, 2f)),
				new VertexPosition(new Vector3(2f, 2f, 2f)),
				new VertexPosition(new Vector3(-2f, -2f, -2f)),
				new VertexPosition(new Vector3(2f, -2f, -2f)),
				new VertexPosition(new Vector3(-2f, 2f, -2f)),
				new VertexPosition(new Vector3(2f, 2f, -2f))
		};
		return new VertexBuffer(VertexPosition.DESCRIPTION, vertices);
	}

	/**
	 * Release resources.
	 */
	public void destroy() {
		this.vertexBuffer.destroy();
		this.indexBuffer.destroy();
		this.cubemap.destroy();
	}
	
	public VertexBuffer getVertexBuffer() {
		return vertexBuffer;
	}

	public IndexBuffer getIndexBuffer() {
		return indexBuffer;
	}

	public Cubemap getCubemap() {
		return cubemap;
	}

}
