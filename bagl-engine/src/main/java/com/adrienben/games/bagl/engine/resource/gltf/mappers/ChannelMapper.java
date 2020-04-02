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
        return switch (type) {
            case "POSITION" -> Mesh.POSITION_INDEX;
            case "NORMAL" -> Mesh.NORMAL_INDEX;
            case "TANGENT" -> Mesh.TANGENT_INDEX;
            case "TEXCOORD_0" -> Mesh.COORDINATES_INDEX;
            case "JOINTS_0" -> Mesh.JOINTS_IDS_INDEX;
            case "WEIGHTS_0" -> Mesh.JOINTS_WEIGHTS_INDEX;
            default -> -1;
        };
    }
}
