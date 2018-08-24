package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.opengl.texture.Filter;
import com.adrienben.tools.gltf.models.GltfFilter;

/**
 * Map {@link GltfFilter} into {@link Filter}.
 *
 * @author adrien
 */
public class FilterMapper {

    /**
     * Map {@code filter} into a {@link Filter} or throws an {@link UnsupportedOperationException}
     * if {@code filter} is not supported.
     */
    public Filter map(final GltfFilter filter) {
        switch (filter) {
            case NEAREST:
                return Filter.NEAREST;
            case LINEAR:
                return Filter.LINEAR;
            case NEAREST_MIPMAP_NEAREST:
                return Filter.MIPMAP_NEAREST_NEAREST;
            case NEAREST_MIPMAP_LINEAR:
                return Filter.MIPMAP_NEAREST_LINEAR;
            case LINEAR_MIPMAP_NEAREST:
                return Filter.MIPMAP_LINEAR_NEAREST;
            case LINEAR_MIPMAP_LINEAR:
                return Filter.MIPMAP_LINEAR_LINEAR;
            default:
                throw new UnsupportedOperationException("Unsupported filter " + filter);
        }
    }
}
