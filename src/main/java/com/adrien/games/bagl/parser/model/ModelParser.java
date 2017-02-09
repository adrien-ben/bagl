package com.adrien.games.bagl.parser.model;

import com.adrien.games.bagl.rendering.Mesh;

public interface ModelParser {

	/**
	 * Parses a file to create a {@link Mesh}.
	 * @param filePath The path to the file.
	 * @return A {@link Mesh}.
	 */
	Mesh parse(String filePath);
	
}
