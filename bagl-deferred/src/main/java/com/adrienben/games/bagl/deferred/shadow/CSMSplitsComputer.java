package com.adrienben.games.bagl.deferred.shadow;

import com.adrienben.games.bagl.core.math.Vectors;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.deferred.data.SceneRenderData;
import org.joml.Matrix4f;
import org.joml.Spheref;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Compute the view-projection matrices for rendering cascaded shadow maps (CSM).
 * <p>
 * This is done by split the camera frustum into smaller sub frusta and computing the light space bounding boxes
 * of these frusta. The bounding boxes are then used to generate orthogonal projection matrices.
 *
 * @author adrien
 */
public class CSMSplitsComputer {

    private final CSMSplitValuesComputer csmSplitValuesComputer = new CSMSplitValuesComputer();
    private final List<CSMSplit> splits = CollectionUtils.createListWithDefaultValues(ArrayList::new, CascadedShadowMap.CASCADE_COUNT, CSMSplit::new);

    private SceneRenderData sceneRenderData;

    // Buffer used for computation for saving instantiation
    private final Matrix4f matrixBuffer = new Matrix4f();
    private final Vector3f vector3Buffer = new Vector3f();

    /**
     * Generates the cascaded shadow maps view-projection matrices.
     */
    public void computeCSMViewProjections() {
        computeCSMSplitValues();
        splits.forEach(this::updateCSMSplits);
    }

    private void computeCSMSplitValues() {
        final var camera = sceneRenderData.getCamera();
        final var splitValues = csmSplitValuesComputer.computeSplits(CascadedShadowMap.CASCADE_COUNT, camera.getzNear(), camera.getzFar());
        for (int i = 0; i < splitValues.size(); i++) {
            final var nearValue = i == 0 ? 0f : splitValues.get(i - 1);
            final var farValue = splitValues.get(i);
            final CSMSplit split = splits.get(i);
            split.setNearDepth(nearValue);
            split.setFarDepth(farValue);
        }
    }

    private void updateCSMSplits(final CSMSplit split) {
        computeSubFrustum(split);
        computeWorldSpaceBoundingSphere(split);
        computeViewProjection(split);
    }

    private void computeSubFrustum(final CSMSplit split) {
        final var cameraFrustum = sceneRenderData.getCamera().getFrustum();
        final var splitFrustum = split.getFrustum();
        cameraFrustum.clipZ(split.getNearDepth(), split.getFarDepth(), splitFrustum);
    }

    private void computeWorldSpaceBoundingSphere(final CSMSplit split) {
        final var subFrustum = split.getFrustum();
        final var boundingSphere = split.getBoundingSphere();
        subFrustum.computeBoundingSphere(boundingSphere);
    }

    private void computeViewProjection(final CSMSplit split) {
        final var viewProjection = split.getLightsViewProjection();
        final var boundingSphere = split.getBoundingSphere();
        final var viewMatrix = computeLightView(split, matrixBuffer);
        computeProjection(viewProjection, boundingSphere).mulOrthoAffine(viewMatrix);
        alignViewProjectionWithTexels(viewProjection);
    }

    private Matrix4f computeLightView(final CSMSplit split, final Matrix4f destination) {
        final var light = sceneRenderData.getDirectionalLights().get(0);
        final var boundingSphere = split.getBoundingSphere();
        final var lightPosition = vector3Buffer.set(light.getDirection()).normalize().mul(-boundingSphere.r).add(boundingSphere.x, boundingSphere.y, boundingSphere.z);
        return destination.setLookAt(lightPosition.x, lightPosition.y, lightPosition.z, boundingSphere.x, boundingSphere.y, boundingSphere.z, 0f, 1f, 0f);
    }

    private Matrix4f computeProjection(final Matrix4f destination, final Spheref boundingSphere) {
        final float radius = boundingSphere.r;
        final float diameter = 2 * radius;
        return destination.setOrtho(-radius, radius, -radius, radius, 1f, diameter);
    }

    private void alignViewProjectionWithTexels(final Matrix4f viewProjection) {
        final var texelOffset = computeTexelOffset(viewProjection, vector3Buffer);
        final var fixMatrix = matrixBuffer.translation(texelOffset.x(), texelOffset.y(), texelOffset.z());
        fixMatrix.mulAffine(viewProjection, viewProjection);
    }

    private Vector3f computeTexelOffset(final Matrix4f viewProjection, final Vector3f destination) {
        final float halfCascadeResolution = CascadedShadowMap.CASCADE_RESOLUTION * 0.5f;
        final var shadowOrigin = viewProjection.transformPosition(Vectors.VEC3_ZERO, destination).mul(halfCascadeResolution);
        return destination.set((float) Math.round(shadowOrigin.x()) - shadowOrigin.x(), (float) Math.round(shadowOrigin.y()) - shadowOrigin.y(), 0f)
                .div(halfCascadeResolution);
    }

    public CSMSplit getSplit(final int splitIndex) {
        return splits.get(splitIndex);
    }

    public void setSceneRenderData(final SceneRenderData sceneRenderData) {
        this.sceneRenderData = sceneRenderData;
    }
}
