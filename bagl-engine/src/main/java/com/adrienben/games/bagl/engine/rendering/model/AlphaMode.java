package com.adrienben.games.bagl.engine.rendering.model;

import com.adrienben.games.bagl.engine.rendering.material.Material;

/**
 * Alpha mode for {@link Material}.
 * <p>
 * There are 3 modes available:
 * <li>OPAQUE: No transparency</li>
 * <li>MASK: Either fully opaque or fully transparent depending on a mask value</li>
 * <li>BLEND: You can se through the object</li>
 */
public enum AlphaMode {
    OPAQUE, MASK, BLEND
}
