package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.tools.gltf.models.GltfAlphaMode;

/**
 * Map {@link GltfAlphaMode} into {@link AlphaMode}.
 *
 * @author adrien
 */
public class AlphaModeMapper {

    /**
     * Map {@code alphaMode} into a {@link AlphaMode} and throws or {@link UnsupportedOperationException}
     * if {@code alphaMode} is not supported.
     */
    public AlphaMode map(final GltfAlphaMode alphaMode) {
        switch (alphaMode) {
            case OPAQUE:
                return AlphaMode.OPAQUE;
            case MASK:
                return AlphaMode.MASK;
            case BLEND:
                return AlphaMode.BLEND;
            default:
                throw new UnsupportedOperationException("Unsupported alpha mode " + alphaMode);
        }
    }
}
