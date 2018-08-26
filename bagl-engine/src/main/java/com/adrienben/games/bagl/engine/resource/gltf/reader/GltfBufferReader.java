package com.adrienben.games.bagl.engine.resource.gltf.reader;

import com.adrienben.tools.gltf.models.GltfAccessor;
import com.adrienben.tools.gltf.models.GltfBuffer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Function;

/**
 * A reader for {@link GltfBuffer}.
 *
 * @author adrien.
 */
public class GltfBufferReader {

    private final GltfBuffer gltfBuffer;
    private final ByteBuffer data;

    public GltfBufferReader(final GltfBuffer gltfBuffer) {
        this.gltfBuffer = gltfBuffer;
        this.data = ByteBuffer.wrap(gltfBuffer.getData()).order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Read a float referenced by {@code accessor}.
     *
     * @param accessor The data accessor.
     * @param index    The index of the float to read.
     * @return A float.
     */
    public Float readFloat(final GltfAccessor accessor, final int index) {
        return readData(accessor, index, ByteBuffer::getFloat);
    }

    /**
     * Read a {@link Quaternionf} referenced by {@code accessor}.
     *
     * @param accessor The data accessor.
     * @param index    The index of the quaternion to read.
     * @return A new quaternion.
     */
    public Quaternionf readQuaternion(final GltfAccessor accessor, final int index) {
        return readData(accessor, index, data -> new Quaternionf(data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat()));
    }

    /**
     * Read a {@link Vector3f} referenced by {@code accessor}.
     *
     * @param accessor The data accessor.
     * @param index    The index of the vector to read.
     * @return A new vector.
     */
    public Vector3f readVector3(final GltfAccessor accessor, final int index) {
        return readData(accessor, index, data -> new Vector3f(data.getFloat(), data.getFloat(), data.getFloat()));
    }

    /**
     * Read a {@link Matrix4f} referenced by {@code accessor}.
     *
     * @param accessor The data accessor.
     * @param index    The index of the matrix to read.
     * @return A new matrix.
     */
    public Matrix4f readMatrix4(final GltfAccessor accessor, final int index) {
        return readData(accessor, index, data ->
                new Matrix4f(
                        data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(),
                        data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(),
                        data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(),
                        data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat())
        );
    }

    private <T> T readData(final GltfAccessor accessor, final int index, final Function<ByteBuffer, T> readFunction) {
        checkAccessorRights(accessor);
        setBufferPosition(accessor, index);
        return readFunction.apply(data);
    }

    private void checkAccessorRights(final GltfAccessor accessor) {
        if (!gltfBuffer.equals(accessor.getBufferView().getBuffer())) {
            throw new IllegalArgumentException("The passed in accessor cannot access this buffer");
        }
    }

    private void setBufferPosition(final GltfAccessor accessor, final int index) {
        final var bufferView = accessor.getBufferView();
        final int elementByteSize = accessor.getType().getComponentCount() * accessor.getComponentType().getByteSize();
        final int offset = bufferView.getByteOffset() + accessor.getByteOffset() + index * elementByteSize;
        data.position(offset);
    }
}
