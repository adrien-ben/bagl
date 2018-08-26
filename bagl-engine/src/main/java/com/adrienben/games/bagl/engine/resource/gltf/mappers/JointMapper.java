package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.engine.rendering.model.Joint;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.resource.gltf.reader.GltfBufferReader;
import com.adrienben.tools.gltf.models.GltfAccessor;
import com.adrienben.tools.gltf.models.GltfNode;
import com.adrienben.tools.gltf.models.GltfSkin;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Map {@link GltfSkin} contained in {@link GltfNode} into {@link Joint} and insert then in the proper {@link ModelNode}.
 *
 * @author adrien
 */
public class JointMapper {

    private ModelNode[] nodeIndex;

    /**
     * Map the {@link GltfSkin} of {@code gltfNode} into a list of {@link Joint} and
     * insert it in the proper {@link ModelNode} found in {@code nodeIndex}.
     *
     * @param gltfNode  The node containing the skin to map.
     * @param nodeIndex The index of model nodes containing the node in which the joints should be inserted.
     */
    public void map(final GltfNode gltfNode, final ModelNode[] nodeIndex) {
        this.nodeIndex = nodeIndex;
        final var gltfSkin = gltfNode.getSkin();
        if (Objects.nonNull(gltfSkin)) {
            final var gltfSkinJoints = gltfSkin.getJoints();
            final var inverseBindMatrices = mapInverseBindMatrices(gltfSkin);
            final var joints = mapJoints(gltfSkinJoints, inverseBindMatrices);
            nodeIndex[gltfNode.getIndex()].setJoints(joints);
        }
    }

    private List<Matrix4f> mapInverseBindMatrices(final GltfSkin gltfSkin) {
        if (Objects.isNull(gltfSkin.getInverseBindMatrices())) {
            return generateEntityMatrices(gltfSkin.getJoints().size());
        }
        return extractInverseBindMatrices(gltfSkin.getInverseBindMatrices());
    }

    private List<Matrix4f> generateEntityMatrices(final int count) {
        return IntStream.range(0, count).mapToObj(index -> new Matrix4f()).collect(Collectors.toList());
    }

    private List<Matrix4f> extractInverseBindMatrices(final GltfAccessor accessor) {
        final var inverseBindMatricesReader = new GltfBufferReader(accessor.getBufferView().getBuffer());
        return IntStream.range(0, accessor.getCount()).mapToObj(index -> inverseBindMatricesReader.readMatrix4(accessor, index)).collect(Collectors.toList());
    }

    private List<Joint> mapJoints(final List<GltfNode> gltfJoints, final List<Matrix4f> inverseBindMatrices) {
        final var joints = new ArrayList<Joint>();
        for (int i = 0; i < gltfJoints.size(); i++) {
            final var gltfSkinJoint = gltfJoints.get(i);
            final var jointNode = nodeIndex[gltfSkinJoint.getIndex()];
            final var inverseBindMatrix = inverseBindMatrices.get(i);
            joints.add(new Joint(jointNode.getTransform(), inverseBindMatrix));
        }
        return joints;
    }
}
