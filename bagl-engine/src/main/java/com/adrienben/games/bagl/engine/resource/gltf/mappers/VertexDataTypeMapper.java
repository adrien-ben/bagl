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
        switch (componentType) {
            case BYTE:
            case UNSIGNED_BYTE:
                return DataType.BYTE;
            case SHORT:
            case UNSIGNED_SHORT:
                return DataType.SHORT;
            case UNSIGNED_INT:
                return DataType.INT;
            case FLOAT:
                return DataType.FLOAT;
            default:
                throw new UnsupportedOperationException("Unsupported component type " + componentType);
        }
    }
}
