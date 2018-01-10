package com.adrien.games.bagl.parser.model;

import com.adrien.games.bagl.rendering.Model;

public interface ModelParser {

    /**
     * Parses a file to create a {@link Model}.
     *
     * @param filePath The path to the file.
     * @return A {@link Model}.
     */
    Model parse(String filePath);

}
