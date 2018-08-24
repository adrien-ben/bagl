package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.opengl.DataType;
import com.adrienben.tools.gltf.models.GltfComponentType;

/**
 * Map {@link GltfComponentType} into {@link DataType}.
 *
 * @author adrien
 */
public class DataTypeMapper {

    /**
     * Map {@code componentType} into a {@link DataType} and throws or {@link UnsupportedOperationException}
     * if {@code componentType} is not supported.
     */
    public DataType map(final GltfComponentType componentType) {
        switch (componentType) {
            case BYTE:
                return DataType.BYTE;
            case UNSIGNED_BYTE:
                return DataType.UNSIGNED_BYTE;
            case SHORT:
                return DataType.SHORT;
            case UNSIGNED_SHORT:
                return DataType.UNSIGNED_SHORT;
            case UNSIGNED_INT:
                return DataType.UNSIGNED_INT;
            case FLOAT:
                return DataType.FLOAT;
            default:
                throw new UnsupportedOperationException("Unsupported component type " + componentType);
        }
    }
}
