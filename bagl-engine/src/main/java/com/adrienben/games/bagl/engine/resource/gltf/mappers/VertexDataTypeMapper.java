package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.opengl.DataType;
import com.adrienben.tools.gltf.models.GltfComponentType;

/**
 * Map {@link GltfComponentType} into {@link DataType} for vertex data.
 *
 * @author adrien
 */
public class VertexDataTypeMapper {

    /**
     * Map {@code componentType} into a {@link DataType} and throws or {@link UnsupportedOperationException}
     * if {@code componentType} is not supported.
     */
    public DataType map(final GltfComponentType componentType) {
        return switch (componentType) {
            case BYTE, UNSIGNED_BYTE -> DataType.BYTE;
            case SHORT, UNSIGNED_SHORT -> DataType.SHORT;
            case UNSIGNED_INT -> DataType.INT;
            case FLOAT -> DataType.FLOAT;
            default -> throw new UnsupportedOperationException("Unsupported component type " + componentType);
        };
    }
}
