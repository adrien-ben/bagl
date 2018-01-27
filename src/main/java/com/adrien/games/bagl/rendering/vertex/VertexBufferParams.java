package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.DataType;
import com.adrien.games.bagl.utils.AssertUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Vertex buffer parameters
 * <p>
 * This class contains parameters needed to build a
 * vertex buffer
 * <p>
 * The available parameters are :
 * <ul>
 * <li>interleaved (boolean) : default = true
 * <li>dataType ({@link DataType} : default = {@link DataType#FLOAT}
 * <li>usage {@link BufferUsage} : default {@link BufferUsage#STATIC_DRAW}
 * <li>elements (list of {@link VertexElement} : empty
 * </ul>
 * <p>
 * The interleaved flag relates to the layout of vertex element in gpu memory.
 * <br>For example lets take a buffer with positions and normals data with both
 * position and normal containing a x and a y float values. If the
 * flag is set to true then elements have to be laid out as follows :
 * <br> p1x|p1y|n1x|n1y|p2x|p2y|...|pNx|pNy|nNx|nNy
 * <br>
 * <br> But if it is set to false element have to be stored as follows :
 * <br> p1x|p1y|p2x|p2y|...pNx|pNy|n1x|n1y|n2x|n2y|...nNx|nNy
 * <p>
 * <br>
 * Use example :
 * <pre>
 * final VertexBufferParams params = VertexBufferParams.builder()
 *     .interleaved(true)
 *     .dataType(DataType.FLOAT)
 *     .usage(BufferUsage.STATIC_DRAW)
 *     .element(new VertexElement(0, 2, false))
 *     .element(new VertexElement(1, 2, true))
 *     .element(new VertexElement(2, 1))
 *     .build();
 * </pre>
 *
 * @author adrien
 */
public final class VertexBufferParams {

    private final boolean interleaved;
    private final DataType dataType;
    private final BufferUsage usage;
    private final List<VertexElement> elements;

    private VertexBufferParams(final Builder builder) {
        this.interleaved = builder.interleaved;
        this.dataType = builder.dataType;
        this.usage = builder.usage;
        this.elements = Collections.unmodifiableList(AssertUtils.validate(builder.elements, c -> !c.isEmpty(),
                "A vertex buffer parameters must have elements"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isInterleaved() {
        return this.interleaved;
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public BufferUsage getUsage() {
        return this.usage;
    }

    public List<VertexElement> getElements() {
        return this.elements;
    }

    /**
     * Vertex buffer parameters builder
     */
    public static class Builder {

        private boolean interleaved = true;
        private DataType dataType = DataType.FLOAT;
        private BufferUsage usage = BufferUsage.STATIC_DRAW;
        private List<VertexElement> elements = new ArrayList<>();

        private Builder() {
            // empty to prevent instantiation
        }

        /**
         * Build a {@link VertexBufferParams}
         *
         * @return A new instance
         */
        public VertexBufferParams build() {
            return new VertexBufferParams(this);
        }

        public Builder interleaved(final boolean interleaved) {
            this.interleaved = interleaved;
            return this;
        }

        public Builder dataType(final DataType dataType) {
            this.dataType = Objects.requireNonNull(dataType);
            return this;
        }

        public Builder usage(final BufferUsage usage) {
            this.usage = Objects.requireNonNull(usage);
            return this;
        }

        /**
         * Add a {@link VertexElement}
         * <p>
         * The order in which the element are added is important !
         *
         * @param element The element to add
         * @return This
         */
        public Builder element(final VertexElement element) {
            this.elements.add(element);
            return this;
        }
    }
}
