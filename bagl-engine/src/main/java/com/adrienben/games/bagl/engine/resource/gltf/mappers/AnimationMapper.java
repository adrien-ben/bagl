package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.engine.animation.Animation;
import com.adrienben.games.bagl.engine.animation.NodeAnimator;
import com.adrienben.games.bagl.engine.animation.NodeKeyFrame;
import com.adrienben.games.bagl.engine.animation.interpolator.LinearNodeKeyFrameInterpolator;
import com.adrienben.games.bagl.engine.animation.interpolator.NodeKeyFrameInterpolator;
import com.adrienben.games.bagl.engine.animation.interpolator.StepNodeKeyFrameInterpolator;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.tools.gltf.models.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Map {@link GltfAnimation} into {@link Animation}.
 *
 * @author adrien
 */
public class AnimationMapper {

    private ModelNode[] nodeIndex;

    /**
     * Map {@code gltfAnimation} into an {@link Animation}.
     *
     * @param gltfAnimation The animation to map.
     * @param nodeIndex     The index containing the nodes referenced in the animation.
     * @return A new {@link Animation}.
     */
    public Animation map(final GltfAnimation gltfAnimation, final ModelNode[] nodeIndex) {
        this.nodeIndex = nodeIndex;
        final var animationNodes = gltfAnimation.getChannels().stream()
                .filter(this::isChannelSupported)
                .map(this::mapChannel)
                .collect(Collectors.toList());
        return new Animation(animationNodes);
    }

    private boolean isChannelSupported(final GltfChannel channel) {
        return channel.getTarget().getPath() != GltfAnimationTargetPath.WEIGHTS
                && channel.getSampler().getInterpolation() != GltfInterpolationType.CUBICSPLINE;
    }

    private NodeAnimator mapChannel(final GltfChannel gltfChannel) {
        final var nodeKeyFrames = mapGltfAnimationSampler(gltfChannel.getSampler(), gltfChannel.getTarget().getPath());
        final var nodeKeyFrameInterpolator = mapNodeKeyFrameInterpolator(gltfChannel.getSampler().getInterpolation());
        final var target = nodeIndex[gltfChannel.getTarget().getNode().getIndex()];
        return new NodeAnimator(target.getLocalTransform(), nodeKeyFrames, nodeKeyFrameInterpolator);
    }

    private List<NodeKeyFrame> mapGltfAnimationSampler(final GltfAnimationSampler gltfAnimationSampler, GltfAnimationTargetPath path) {
        final GltfAccessor input = gltfAnimationSampler.getInput();
        final GltfAccessor output = gltfAnimationSampler.getOutput();
        final List<NodeKeyFrame> nodeKeyFrames = new ArrayList<>();
        for (int i = 0; i < input.getCount(); i++) {
            final float time = readFloat(input, i);
            final var keyFrame = new NodeKeyFrame(time);
            readAnimationOutput(output, i, keyFrame, path);
            nodeKeyFrames.add(keyFrame);
        }
        return nodeKeyFrames;
    }

    private NodeKeyFrameInterpolator mapNodeKeyFrameInterpolator(final GltfInterpolationType gltfInterpolationType) {
        switch (gltfInterpolationType) {
            case STEP:
                return new StepNodeKeyFrameInterpolator();
            case LINEAR:
                return new LinearNodeKeyFrameInterpolator();
            default:
                throw new UnsupportedOperationException("Unsupported interpolation type " + gltfInterpolationType);
        }
    }

    private float readFloat(final GltfAccessor gltfAccessor, final int index) {
        final var bufferView = gltfAccessor.getBufferView();
        final var data = bufferView.getBuffer().getData();
        return ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getFloat(bufferView.getByteOffset() + gltfAccessor.getByteOffset() + index * 4);
    }

    private void readAnimationOutput(final GltfAccessor gltfAccessor, final int index, final NodeKeyFrame nodeKeyFrame, final GltfAnimationTargetPath path) {
        switch (path) {
            case TRANSLATION:
                nodeKeyFrame.setTranslation(readVector3(gltfAccessor, index));
                return;
            case ROTATION:
                nodeKeyFrame.setRotation(readQuaternions(gltfAccessor, index));
                return;
            case SCALE:
                nodeKeyFrame.setScale(readVector3(gltfAccessor, index));
                return;
            default:
                throw new UnsupportedOperationException("Unsupported animation target path");
        }
    }

    private Vector3f readVector3(final GltfAccessor gltfAccessor, final int index) {
        final var bufferView = gltfAccessor.getBufferView();
        final int elementByteSize = gltfAccessor.getType().getComponentCount() * gltfAccessor.getComponentType().getByteSize();
        final int offset = bufferView.getByteOffset()
                + gltfAccessor.getByteOffset()
                + index * elementByteSize;
        final var bufferWrapper = ByteBuffer.wrap(bufferView.getBuffer().getData()).order(ByteOrder.LITTLE_ENDIAN).position(offset);
        return new Vector3f(bufferWrapper.getFloat(), bufferWrapper.getFloat(), bufferWrapper.getFloat());
    }

    private Quaternionf readQuaternions(final GltfAccessor gltfAccessor, final int index) {
        final var bufferView = gltfAccessor.getBufferView();
        final int elementByteSize = gltfAccessor.getType().getComponentCount() * gltfAccessor.getComponentType().getByteSize();
        final int offset = bufferView.getByteOffset()
                + gltfAccessor.getByteOffset()
                + index * elementByteSize;
        final var bufferWrapper = ByteBuffer.wrap(bufferView.getBuffer().getData()).order(ByteOrder.LITTLE_ENDIAN).position(offset);
        return new Quaternionf(bufferWrapper.getFloat(), bufferWrapper.getFloat(), bufferWrapper.getFloat(), bufferWrapper.getFloat()
        );
    }
}
