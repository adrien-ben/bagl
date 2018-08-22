package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.tools.gltf.models.GltfNode;
import com.adrienben.tools.gltf.models.GltfQuaternion;
import com.adrienben.tools.gltf.models.GltfVec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Map {@link GltfNode} into {@link ModelNode}.
 *
 * @author adrien.
 */
public class ModelNodeMapper {

    /**
     * Map {@code gltfNode} into {@code destination}.
     * <p>
     * The mapping is NOT recursive. The children are not mapped.
     *
     * @param gltfNode    The node to map.
     * @param destination The destination of the mapping.
     * @param meshIndex   The index meshes containing all meshes.
     * @return {@code destination}.
     */
    public ModelNode map(final GltfNode gltfNode, final ModelNode destination, final List<Map<Mesh, Material>> meshIndex) {
        destination.getLocalTransform().set(mapTransform(gltfNode));
        if (Objects.nonNull(gltfNode.getMesh())) {
            meshIndex.get(gltfNode.getMesh().getIndex()).forEach(destination::addMesh);
        }
        return destination;
    }

    /**
     * Map a node's transform
     *
     * @param gltfNode The node whose map has to be mapped
     * @return A new transform
     */
    private Transform mapTransform(final GltfNode gltfNode) {
        final var transform = new Transform();
        transform.setTranslation(mapGltfVec3(gltfNode.getTranslation()));
        transform.setRotation(mapGltfQuaternion(gltfNode.getRotation()));
        transform.setScale(mapGltfVec3(gltfNode.getScale()));
        return transform;
    }

    /**
     * Map a vector3
     *
     * @param vec3 The vector to map
     * @return A new vector
     */
    private Vector3f mapGltfVec3(final GltfVec3 vec3) {
        return new Vector3f(vec3.getX(), vec3.getY(), vec3.getZ());
    }

    /**
     * Map a quaternion
     *
     * @param quaternion The quaternion to map
     * @return A new quaternion
     */
    private Quaternionf mapGltfQuaternion(final GltfQuaternion quaternion) {
        return new Quaternionf(quaternion.getI(), quaternion.getJ(), quaternion.getK(), quaternion.getA());
    }
}
