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
        return switch (alphaMode) {
            case OPAQUE -> AlphaMode.OPAQUE;
            case MASK -> AlphaMode.MASK;
            case BLEND -> AlphaMode.BLEND;
        };
    }
}
