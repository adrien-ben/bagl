package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.opengl.PrimitiveType;
import com.adrienben.tools.gltf.models.GltfPrimitiveMode;

/**
 * Map {@link GltfPrimitiveMode} into {@link PrimitiveType}.
 *
 * @author adrien
 */
public class PrimitiveTypeMapper {

    /**
     * Map {@code mode} into a {@link PrimitiveType} and throws or {@link UnsupportedOperationException}
     * if {@code mode} is not supported.
     */
    public PrimitiveType map(final GltfPrimitiveMode mode) {
        return switch (mode) {
            case POINTS -> PrimitiveType.POINTS;
            case TRIANGLES -> PrimitiveType.TRIANGLES;
            case TRIANGLE_STRIP -> PrimitiveType.TRIANGLE_STRIP;
            default -> throw new UnsupportedOperationException("Unsupported primitive type " + mode);
        };
    }
}
