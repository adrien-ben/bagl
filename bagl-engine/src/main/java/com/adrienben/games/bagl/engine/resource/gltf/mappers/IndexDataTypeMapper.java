package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.opengl.DataType;
import com.adrienben.tools.gltf.models.GltfComponentType;

/**
 * Map {@link GltfComponentType} into {@link DataType} for index data.
 *
 * @author adrien
 */
public class IndexDataTypeMapper {

    /**
     * Map {@code componentType} into a {@link DataType} and throws or {@link UnsupportedOperationException}
     * if {@code componentType} is not supported.
     */
    public DataType map(final GltfComponentType componentType) {
        return switch (componentType) {
            case BYTE -> DataType.BYTE;
            case UNSIGNED_BYTE -> DataType.UNSIGNED_BYTE;
            case SHORT -> DataType.SHORT;
            case UNSIGNED_SHORT -> DataType.UNSIGNED_SHORT;
            case UNSIGNED_INT -> DataType.UNSIGNED_INT;
            case FLOAT -> DataType.FLOAT;
        };
    }
}
