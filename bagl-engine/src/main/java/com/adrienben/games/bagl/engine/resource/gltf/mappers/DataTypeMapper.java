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
    public DataType map(final GltfComponentType componentType, final boolean isIndex) {
        switch (componentType) {
            case BYTE:
                return DataType.BYTE;
            case UNSIGNED_BYTE:
                return isIndex ? DataType.UNSIGNED_BYTE : DataType.BYTE;
            case SHORT:
                return DataType.SHORT;
            case UNSIGNED_SHORT:
                return isIndex ? DataType.UNSIGNED_SHORT : DataType.SHORT;
            case UNSIGNED_INT:
                return isIndex ? DataType.UNSIGNED_INT : DataType.INT;
            case FLOAT:
                return DataType.FLOAT;
            default:
                throw new UnsupportedOperationException("Unsupported component type " + componentType);
        }
    }
}
