package com.adrienben.games.bagl.engine.resource.gltf.reader;

import com.adrienben.tools.gltf.models.GltfAccessor;

/**
 * Define a function to read data referenced by a {@link GltfAccessor} using a {@link GltfBufferReader}.
 *
 * @param <D> The type of data to read.
 */
@FunctionalInterface
public interface GltfDataReader<D> {

    /**
     * Read the data referenced by {@code accessor} using {@code bufferReader}.
     *
     * @param bufferReader The gltf buffer reader.
     * @param accessor     The accessor referencing the data to read.
     * @param index        The index of the element to read in the accessor.
     * @return The read data.
     */
    D read(GltfBufferReader bufferReader, GltfAccessor accessor, Integer index);
}
