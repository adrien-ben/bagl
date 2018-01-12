package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.DataType;

import java.util.ArrayList;
import java.util.List;

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
 * flag is set to true then elements will be laid out as follows :
 * <br> p1x|p1y|n1x|n1y|p2x|p2y|...|pNx|pNy|nNx|nNy
 * <br>
 * <br> But if it is set to false element will be stored as follows :
 * <br> p1x|p1y|p2x|p2y|...pNx|pNy|n1x|n1y|n2x|n2y|...nNx|nNy
 * <p>
 * <br>
 * Use example :
 * <pre>
 * <code>
 * final VertexBufferParams params = new VertexBufferParams()
 *     .interleaved(true)
 *     .dataType(DataType.FLOAT)
 *     .usage(BufferUsage.STATIC_DRAW)
 *     .element(new VertexElement(0, 2, false))
 *     .element(new VertexElement(1, 2, true))
 *     .element(new VertexElement(2, 1));
 * </code>
 * </pre>
 *
 * @author adrien
 */
public class VertexBufferParams {

    private boolean interleaved = true;
    private DataType dataType = DataType.FLOAT;
    private BufferUsage usage = BufferUsage.STATIC_DRAW;
    private List<VertexElement> elements = new ArrayList<>();

    public VertexBufferParams interleaved(final boolean interleaved) {
        this.interleaved = interleaved;
        return this;
    }

    public boolean isInterleaved() {
        return this.interleaved;
    }

    public VertexBufferParams dataType(final DataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public VertexBufferParams usage(final BufferUsage usage) {
        this.usage = usage;
        return this;
    }

    public BufferUsage getUsage() {
        return this.usage;
    }

    public VertexBufferParams element(final VertexElement element) {
        this.elements.add(element);
        return this;
    }

    public List<VertexElement> getElements() {
        return this.elements;
    }
}
