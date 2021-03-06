package com.adrienben.games.bagl.engine.rendering.model;

import com.adrienben.games.bagl.engine.Transform;
import org.joml.Matrix4f;

/**
 * Mesh skinning joint.
 * <p>
 * Represent the transformation to apply to mesh vertices to perform mesh skinning.
 *
 * @author adrien
 */
public class Joint {

    private final Transform tranform;
    private final Matrix4f inverseBindMatrix;
    private final Matrix4f jointMatrix = new Matrix4f();

    /**
     * Construct a joint.
     *
     * @param tranform          The global transform of the node.
     * @param inverseBindMatrix The inverse bind matrix used to bring vertices to the node space.
     */
    public Joint(final Transform tranform, final Matrix4f inverseBindMatrix) {
        this.tranform = tranform;
        this.inverseBindMatrix = inverseBindMatrix;
    }

    /***
     * Compute the actual matrix to apply the the skinned vertices.
     *
     * @param meshGlobalTransform The global transform of the mesh.
     */
    public void computeJointMatrix(final Transform meshGlobalTransform) {
        meshGlobalTransform.getTransformMatrix().invert(jointMatrix);
        jointMatrix.mul(tranform.getTransformMatrix());
        jointMatrix.mul(inverseBindMatrix);
    }

    public Matrix4f getJointMatrix() {
        return jointMatrix;
    }
}
