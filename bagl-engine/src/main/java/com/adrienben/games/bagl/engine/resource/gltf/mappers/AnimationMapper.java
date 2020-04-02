package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.animation.*;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.resource.gltf.reader.GltfBufferReader;
import com.adrienben.games.bagl.engine.resource.gltf.reader.GltfDataReader;
import com.adrienben.tools.gltf.models.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Map {@link GltfAnimation} into {@link Animation}.
 * <p>
 * Weight animation and cubic spline interpolation are ignored.
 *
 * @author adrien
 */
public class AnimationMapper {

    private Animation.Builder<Transform> animationBuilder;
    private ModelNode[] nodeIndex;

    /**
     * Map {@code gltfAnimation} into an {@link Animation}.
     *
     * @param gltfAnimation The animation to map.
     * @param nodeIndex     The index containing the nodes referenced in the animation.
     * @return A new {@link Animation}.
     */
    public Animation<Transform> map(final GltfAnimation gltfAnimation, final ModelNode[] nodeIndex) {
        this.nodeIndex = nodeIndex;
        animationBuilder = Animation.builder();
        gltfAnimation.getChannels().stream()
                .filter(this::isChannelSupported)
                .forEach(this::mapAnimator);
        return animationBuilder.build();
    }

    private boolean isChannelSupported(final GltfChannel channel) {
        return channel.getTarget().getPath() != GltfAnimationTargetPath.WEIGHTS
                && channel.getSampler().getInterpolation() != GltfInterpolationType.CUBICSPLINE;
    }

    private void mapAnimator(final GltfChannel gltfChannel) {
        switch (gltfChannel.getTarget().getPath()) {
            case TRANSLATION -> animationBuilder.animator(Vector3f.class, mapTranslationAnimator(gltfChannel));
            case ROTATION -> animationBuilder.animator(Quaternionf.class, mapRotationAnimator(gltfChannel));
            case SCALE -> animationBuilder.animator(Vector3f.class, mapScaleAnimator(gltfChannel));
            default -> throw new UnsupportedOperationException("Unsupported animation target path");
        }
    }

    private Animator<Transform, Vector3f> mapTranslationAnimator(final GltfChannel channel) {
        return mapAnimator(channel, GltfBufferReader::readVector3, TargetUpdater.transformTranslationUpdater(),
                this::getVector3fInterpolation, Vector3f::new);
    }

    private Animator<Transform, Quaternionf> mapRotationAnimator(final GltfChannel channel) {
        return mapAnimator(channel, GltfBufferReader::readQuaternion, TargetUpdater.transformRotationUpdater(),
                this::getQuaternionfInterpolation, Quaternionf::new);
    }

    private Animator<Transform, Vector3f> mapScaleAnimator(final GltfChannel channel) {
        return mapAnimator(channel, GltfBufferReader::readVector3, TargetUpdater.translationScaleUpdater(),
                this::getVector3fInterpolation, Vector3f::new);
    }

    private Interpolator<Vector3f> getVector3fInterpolation(final GltfInterpolationType gltfInterpolationType) {
        return switch (gltfInterpolationType) {
            case STEP -> Interpolator.vector3fStep();
            case LINEAR -> Interpolator.vector3fLerp();
            default -> throw new UnsupportedOperationException("Unsupported interpolation type " + gltfInterpolationType);
        };
    }

    private Interpolator<Quaternionf> getQuaternionfInterpolation(final GltfInterpolationType gltfInterpolationType) {
        return switch (gltfInterpolationType) {
            case STEP -> Interpolator.quaternionfStep();
            case LINEAR -> Interpolator.quaternionfSlerp();
            default -> throw new UnsupportedOperationException("Unsupported interpolation type " + gltfInterpolationType);
        };
    }

    private <T> Animator<Transform, T> mapAnimator(
            final GltfChannel gltfChannel,
            final GltfDataReader<T> dataReader,
            final TargetUpdater<Transform, T> targetUpdater,
            final Function<GltfInterpolationType, Interpolator<T>> interpolatorMapper,
            final Supplier<T> defaultValueSupplier
    ) {
        final var gltfSampler = gltfChannel.getSampler();
        final var interpolation = gltfSampler.getInterpolation();
        return mapTargetAndKeyFrames(gltfChannel, dataReader)
                .interpolator(interpolatorMapper.apply(interpolation))
                .targetUpdater(targetUpdater)
                .currentValueSupplier(defaultValueSupplier)
                .build();
    }

    private <T> Animator.Builder<Transform, T> mapTargetAndKeyFrames(final GltfChannel gltfChannel, final GltfDataReader<T> dataReader) {
        final var gltfSampler = gltfChannel.getSampler();
        final var gltfTarget = gltfChannel.getTarget();
        final var target = nodeIndex[gltfTarget.getNode().getIndex()].getLocalTransform();
        final var keyFrames = mapKeyFrames(gltfSampler.getInput(), gltfSampler.getOutput(), dataReader);
        return Animator.<Transform, T>builder().target(target).keyFrames(keyFrames);
    }

    private <T> List<KeyFrame<T>> mapKeyFrames(final GltfAccessor input, final GltfAccessor output, final GltfDataReader<T> dataReader) {
        final var inputReader = new GltfBufferReader(input.getBufferView().getBuffer());
        final var outputReader = new GltfBufferReader(output.getBufferView().getBuffer());
        final var keyFrames = new ArrayList<KeyFrame<T>>();
        for (int i = 0; i < input.getCount(); i++) {
            final float time = inputReader.readFloat(input, i);
            final T data = dataReader.read(outputReader, output, i);
            keyFrames.add(new KeyFrame<>(time, data));
        }
        return keyFrames;
    }
}
