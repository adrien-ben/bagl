package com.adrienben.games.bagl.opengl.vertex;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.opengl.DataType;
import com.adrienben.games.bagl.opengl.buffer.Buffer;
import com.adrienben.games.bagl.opengl.buffer.BufferTarget;

import java.nio.*;

/**
 * Vertex buffer
 *
 * @author adrien
 */
public class VertexBuffer {

    private static int boundBuffer = 0;

    private final Buffer buffer;
    private final int vertexCount;
    private final int bufferSize;
    private final int stride;
    private final VertexBufferParams params;

    /**
     * Construct a vertex buffer
     * <p>
     * dataType must be {@link DataType#DOUBLE}
     *
     * @param data   The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final DoubleBuffer data, final VertexBufferParams params) {
        this.checkType(params.getDataType(), DataType.DOUBLE, "DoubleBuffer is only compatible with DataType.DOUBLE");

        this.vertexCount = this.checkBufferConsistenceWithParams(data, DataType.DOUBLE, params);
        this.bufferSize = data.capacity();
        this.stride = this.computeStride(params);
        this.params = params;
        this.buffer = new Buffer(data, params.getUsage());
    }


    /**
     * Construct a vertex buffer
     * <p>
     * dataType must be {@link DataType#FLOAT}
     *
     * @param data   The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final FloatBuffer data, final VertexBufferParams params) {
        this.checkType(params.getDataType(), DataType.FLOAT, "FloatBuffer is only compatible with DataType.FLOAT");

        this.vertexCount = this.checkBufferConsistenceWithParams(data, DataType.FLOAT, params);
        this.bufferSize = data.capacity();
        this.stride = this.computeStride(params);
        this.params = params;
        this.buffer = new Buffer(data, params.getUsage());
    }

    /**
     * Construct a vertex buffer
     * <p>
     * dataType must be {@link DataType#INT}
     *
     * @param data   The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final IntBuffer data, final VertexBufferParams params) {
        this.checkType(params.getDataType(), DataType.INT, "IntBuffer is only compatible with DataType.INT");

        this.vertexCount = this.checkBufferConsistenceWithParams(data, DataType.INT, params);
        this.bufferSize = data.capacity();
        this.stride = this.computeStride(params);
        this.params = params;
        this.buffer = new Buffer(data, params.getUsage());
    }

    /**
     * Construct a vertex buffer
     * <p>
     * dataType must be {@link DataType#SHORT}
     *
     * @param data   The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final ShortBuffer data, final VertexBufferParams params) {
        this.checkType(params.getDataType(), DataType.SHORT, "ShortBuffer is only compatible with DataType.SHORT");

        this.vertexCount = this.checkBufferConsistenceWithParams(data, DataType.SHORT, params);
        this.bufferSize = data.capacity();
        this.stride = this.computeStride(params);
        this.params = params;
        this.buffer = new Buffer(data, params.getUsage());
    }

    /**
     * Construct a vertex buffer
     * <p>
     * Unlike other constructor this one allows and {@code dataType}.
     *
     * @param data   The vertex data
     * @param params The buffer parameters
     */
    public VertexBuffer(final ByteBuffer data, final VertexBufferParams params) {
        this.vertexCount = this.checkBufferConsistenceWithParams(data, DataType.BYTE, params);
        this.bufferSize = data.capacity() / params.getDataType().getSize();
        this.stride = this.computeStride(params);
        this.params = params;
        this.buffer = new Buffer(data, params.getUsage());
    }

    /**
     * Compute buffer stride
     * <p>
     * The stride is the number of bytes between the start of two
     * elements (positions for example) in the buffer. If vertex
     * elements are packed the stride is 0. Otherwise it it the
     * sum of the sizes of the {@link VertexElement} of the buffer.
     *
     * @param params The parameters of the buffer
     * @return The stride of the buffer
     */
    private int computeStride(final VertexBufferParams params) {
        return params.isInterleaved()
                ? params.getElements().stream().mapToInt(VertexElement::getSize).sum() * params.getDataType().getSize()
                : 0;
    }

    /**
     * Check that the data type of the buffer match the expected data type
     *
     * @param dataType The data type of the buffer
     * @param expected The expected data type
     * @param message  The error message if they don't match
     */
    private void checkType(final DataType dataType, final DataType expected, final String message) {
        if (!dataType.equals(expected)) {
            throw new EngineException(message);
        }
    }

    /**
     * Check that buffer size is consistent with buffer parameters
     *
     * @param buffer         The buffer to check
     * @param bufferDataType The data type of elements in the buffer
     * @param params         The parameters of the buffer
     * @return The number of vertex contained in the buffer
     */
    private int checkBufferConsistenceWithParams(
            final java.nio.Buffer buffer,
            final DataType bufferDataType,
            final VertexBufferParams params
    ) {
        final var capacity = buffer.capacity() / (params.getDataType().getSize() / bufferDataType.getSize());
        final var vertexSize = params.getElements().stream().mapToInt(VertexElement::getSize).sum();
        if (capacity % vertexSize != 0) {
            throw new EngineException("The number of elements in the buffer (" + capacity + ") is incorrect. It should be a multiple of "
                    + vertexSize + " (sum of the sizes of the vertex elements)");
        }
        return capacity / vertexSize;
    }

    /**
     * Release resources
     * <p>
     * Unbinds the buffer if it is still bound
     */
    public void destroy() {
        if (VertexBuffer.boundBuffer == buffer.getHandle()) {
            bind(0);
        }
        buffer.destroy();
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#DOUBLE}
     *
     * @param data The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final DoubleBuffer data) {
        checkType(params.getDataType(), DataType.DOUBLE, "DoubleBuffer is only compatible with DataType.DOUBLE. Current data type is "
                + params.getDataType());
        checkUpdateBufferSize(data);
        buffer.setSubData(data, 0);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#FLOAT}
     *
     * @param data The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final FloatBuffer data) {
        checkType(params.getDataType(), DataType.FLOAT, "FloatBuffer is only compatible with DataType.FLOAT. Current data type is "
                + params.getDataType());
        checkUpdateBufferSize(data);
        buffer.setSubData(data, 0);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#INT}
     *
     * @param data The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final IntBuffer data) {
        checkType(params.getDataType(), DataType.INT, "IntBuffer is only compatible with DataType.INT. Current data type is "
                + params.getDataType());
        checkUpdateBufferSize(data);
        buffer.setSubData(data, 0);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#SHORT}
     *
     * @param data The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final ShortBuffer data) {
        checkType(params.getDataType(), DataType.SHORT, "ShortBuffer is only compatible with DataType.SHORT. Current data type is "
                + params.getDataType());
        checkUpdateBufferSize(data);
        buffer.setSubData(data, 0);
    }

    /**
     * Update the vertex buffer data
     * <p>
     * dataType must be {@link DataType#BYTE}
     *
     * @param data The vertex data
     * @throws EngineException if the buffer is not bound
     */
    public void update(final ByteBuffer data) {
        checkUpdateBufferSize(data);
        buffer.setSubData(data, 0);
    }

    /**
     * Check that the size of the buffer used when updating is not bigger than
     * the size of the buffer
     *
     * @param buffer The update buffer
     */
    private void checkUpdateBufferSize(final java.nio.Buffer buffer) {
        if (buffer.capacity() / params.getDataType().getSize() > bufferSize) {
            throw new EngineException("The buffer has too much element. Max size is " + bufferSize);
        }
    }

    /**
     * Bind the buffer
     */
    public void bind() {
        if (boundBuffer != buffer.getHandle()) {
            bind(buffer.getHandle());
        }
    }

    /**
     * Unbind the buffer
     *
     * @throws EngineException if the buffer is not bound
     */
    public void unbind() {
        checkIsBound("You cannot unbind a vertex buffer which is not bound");
        bind(0);
    }

    /**
     * Bind a buffer ou unbind any buffer if vboId is 0
     *
     * @param vboId The id of the buffer to bind
     */
    private void bind(final int vboId) {
        buffer.bind(BufferTarget.ARRAY);
        boundBuffer = vboId;
    }

    /**
     * Check that the buffer is bound
     *
     * @throws EngineException if the buffer is not bound
     */
    private void checkIsBound(final String message) {
        if (boundBuffer != buffer.getHandle()) {
            throw new EngineException(message);
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getStride() {
        return stride;
    }

    public VertexBufferParams getParams() {
        return params;
    }
}
