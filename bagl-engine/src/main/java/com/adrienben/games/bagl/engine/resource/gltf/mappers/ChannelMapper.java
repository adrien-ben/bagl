package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.engine.rendering.model.Mesh;

/**
 * Map channel type string into a vertex attribute channel index.
 *
 * @author adrien
 */
public class ChannelMapper {

    /**
     * Retrieve the vertex attribute channel from the type of attribute {@code type}.
     * <p>
     * If the type of attribute is not supported it returns -1.
     */
    public int map(final String type) {
        switch (type) {
            case "POSITION":
                return Mesh.POSITION_INDEX;
            case "NORMAL":
                return Mesh.NORMAL_INDEX;
            case "TANGENT":
                return Mesh.TANGENT_INDEX;
            case "TEXCOORD_0":
                return Mesh.COORDINATES_INDEX;
            default:
                return -1;
        }
    }
}
