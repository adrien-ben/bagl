package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.opengl.texture.Wrap;
import com.adrienben.tools.gltf.models.GltfWrapMode;

/**
 * Map {@link GltfWrapMode} into {@link Wrap}.
 *
 * @author adrien
 */
public class WrapMapper {

    /**
     * Map {@code wrapMode} into a {@link Wrap} or throws an {@link UnsupportedOperationException}
     * if {@code wrapMode} is not supported.
     */
    public Wrap map(final GltfWrapMode wrapMode) {
        switch (wrapMode) {
            case REPEAT:
                return Wrap.REPEAT;
            case CLAMP_TO_EDGE:
                return Wrap.CLAMP_TO_EDGE;
            case MIRRORED_REPEAT:
                return Wrap.MIRRORED_REPEAT;
            default:
                throw new UnsupportedOperationException("Unsupported wrap mode " + wrapMode);
        }
    }
}
