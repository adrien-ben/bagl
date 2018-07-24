package com.adrien.games.bagl.assets;

import com.adrien.games.bagl.utils.repository.DefaultRepository;

import java.util.Map;

/**
 * A repository of {@link AssetDescriptor}.
 *
 * @author adrien
 */
public class AssetDescriptorRepository extends DefaultRepository<String, AssetDescriptor> {

    public AssetDescriptorRepository(final Map<String, AssetDescriptor> assetDescriptors) {
        assetDescriptors.forEach(super::put);
    }


}
