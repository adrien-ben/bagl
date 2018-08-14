package com.adrienben.games.bagl.deferred.shadow;

import com.adrienben.games.bagl.core.math.Frustum;
import com.adrienben.games.bagl.core.math.Vectors;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.deferred.data.SceneRenderData;
import org.joml.AABBf;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Compute the view-projection matrices for rendering cascaded shadow maps (CSM).
 * <p>
 * This is done by split the camera frustum into smaller sub frusta and computing the light space bounding boxes
 * of these frusta. The bounding boxes are then used to generate orthogonal projection matrices.
 *
 * @author adrien
 */
public class CSMViewProjectionComputer {

    private final CSMSplitValuesComputer csmSplitValuesComputer = new CSMSplitValuesComputer();

    private List<Float> splitValues;
    private SceneRenderData sceneRenderData;
    private Frustum frustum;

    private final Matrix4f lightWorld = new Matrix4f();
    private final List<Frustum> subFrusta = CollectionUtils.createListWithDefaultValues(ArrayList::new, CascadedShadowMap.CASCADE_COUNT, Frustum::new);
    private final List<AABBf> subFrustaBoundingBoxes = CollectionUtils.createListWithDefaultValues(ArrayList::new, CascadedShadowMap.CASCADE_COUNT, AABBf::new);
    private final List<Matrix4f> subFrustaViewProjections = CollectionUtils.createListWithDefaultValues(ArrayList::new, CascadedShadowMap.CASCADE_COUNT, Matrix4f::new);

    /**
     * Generates the cascaded shadow maps view-projection matrices.
     */
    public void computeCSMViewProjections() {
        final var camera = sceneRenderData.getCamera();
        frustum = Objects.requireNonNull(camera).getFrustum();
        splitValues = csmSplitValuesComputer.computeSplits(CascadedShadowMap.CASCADE_COUNT, camera.getzNear(), camera.getzFar());
        generateSubFrusta();
        transposeSubFrusta();
        generateFrustaBoundingBoxes();
        computeFrustaViewProjections();
    }

    private void generateSubFrusta() {
        for (int i = 0; i < splitValues.size(); i++) {
            computeSubFrustum(i);
        }
    }

    private void computeSubFrustum(final int index) {
        final var splitValue = splitValues.get(index);
        final var frustum = subFrusta.get(index);
        this.frustum.clipZ(splitValue, frustum);
    }

    private void transposeSubFrusta() {
        computeLightWorld();
        subFrusta.forEach(subFrusta -> subFrusta.transform(lightWorld));
    }

    private void computeLightWorld() {
        final var light = sceneRenderData.getDirectionalLights().get(0);
        lightWorld.setLookAt(Vectors.VEC3_ZERO, light.getDirection(), Vectors.VEC3_UP);
    }

    private void generateFrustaBoundingBoxes() {
        for (int i = 0; i < subFrusta.size(); i++) {
            generateFrustumBoundingBox(i);
        }
    }

    private void generateFrustumBoundingBox(final int index) {
        final var subFrustum = subFrusta.get(index);
        final var boundingBox = subFrustaBoundingBoxes.get(index);
        subFrustum.computeBoundingBox(boundingBox);
    }

    private void computeFrustaViewProjections() {
        for (int i = 0; i < subFrustaBoundingBoxes.size(); i++) {
            computeSubFrustumViewProjection(i);
        }
    }

    private void computeSubFrustumViewProjection(final int index) {
        final var subFrustumBoundingBox = subFrustaBoundingBoxes.get(index);
        final var subFrustumViewProjection = subFrustaViewProjections.get(index);
        computeProjection(subFrustumViewProjection, subFrustumBoundingBox).mulOrthoAffine(lightWorld);
    }

    private Matrix4f computeProjection(final Matrix4f destination, final AABBf subFrustumBoundingBox) {
        destination.setOrtho(subFrustumBoundingBox.minX, subFrustumBoundingBox.maxX, subFrustumBoundingBox.minY,
                subFrustumBoundingBox.maxY, -subFrustumBoundingBox.maxZ, -subFrustumBoundingBox.minZ);
        return destination;
    }

    public float getSplitValueForSplit(final int splitIndex) {
        return splitValues.get(splitIndex);
    }

    public Matrix4f getViewProjectionForSplit(final int splitIndex) {
        return subFrustaViewProjections.get(splitIndex);
    }

    public void setSceneRenderData(final SceneRenderData sceneRenderData) {
        this.sceneRenderData = sceneRenderData;
    }
}
