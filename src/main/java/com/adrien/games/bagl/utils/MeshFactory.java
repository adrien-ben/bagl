package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.parser.model.ModelParser;
import com.adrien.games.bagl.parser.model.ObjParser;
import com.adrien.games.bagl.rendering.Model;

import java.io.File;

public final class MeshFactory {

    private static final ModelParser parser = new ObjParser();

    private MeshFactory() {
    }

    public static Model fromResourceFile(String resourceName) {
        return parser.parse(new File(MeshFactory.class.getResource(resourceName).getFile()).getAbsolutePath());
    }

    public static Model fromFile(String filepath) {
        return parser.parse(filepath);
    }

}
