package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.Wrap;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPosition;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

/**
 * Skybox mesh
 *
 * @author adrien
 */
public class Skybox {

    private final static float SKYBOX_HALF_SIZE = 0.5f;

    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;
    private final Texture environmentMap;


    /**
     * Create a skybox from an environment map
     * <p>
     * The environment map should be an HDR equirectangular texture. Otherwise,
     * results are not guaranteed
     *
     * @param environmentMapFilePath The path of the file containing the environment map of the texture
     */
    public Skybox(final String environmentMapFilePath) {
        this.vertexBuffer = this.initVertices();
        this.indexBuffer = this.initIndices();
        this.environmentMap = this.loadHDREnvMap(environmentMapFilePath);
    }

    private IndexBuffer initIndices() {
        final int[] indices = new int[]{
                1, 0, 3, 3, 0, 2,
                5, 1, 7, 7, 1, 3,
                4, 5, 6, 6, 5, 7,
                0, 4, 2, 2, 4, 6,
                6, 7, 2, 2, 7, 3,
                0, 1, 4, 4, 1, 5
        };
        return new IndexBuffer(BufferUsage.STATIC_DRAW, indices);
    }

    private VertexBuffer initVertices() {
        final Vertex[] vertices = new VertexPosition[]{
                new VertexPosition(new Vector3(-SKYBOX_HALF_SIZE, -SKYBOX_HALF_SIZE, SKYBOX_HALF_SIZE)),
                new VertexPosition(new Vector3(SKYBOX_HALF_SIZE, -SKYBOX_HALF_SIZE, SKYBOX_HALF_SIZE)),
                new VertexPosition(new Vector3(-SKYBOX_HALF_SIZE, SKYBOX_HALF_SIZE, SKYBOX_HALF_SIZE)),
                new VertexPosition(new Vector3(SKYBOX_HALF_SIZE, SKYBOX_HALF_SIZE, SKYBOX_HALF_SIZE)),
                new VertexPosition(new Vector3(-SKYBOX_HALF_SIZE, -SKYBOX_HALF_SIZE, -SKYBOX_HALF_SIZE)),
                new VertexPosition(new Vector3(SKYBOX_HALF_SIZE, -SKYBOX_HALF_SIZE, -SKYBOX_HALF_SIZE)),
                new VertexPosition(new Vector3(-SKYBOX_HALF_SIZE, SKYBOX_HALF_SIZE, -SKYBOX_HALF_SIZE)),
                new VertexPosition(new Vector3(SKYBOX_HALF_SIZE, SKYBOX_HALF_SIZE, -SKYBOX_HALF_SIZE))
        };
        return new VertexBuffer(VertexPosition.DESCRIPTION, BufferUsage.STATIC_DRAW, vertices);
    }

    private Texture loadHDREnvMap(final String filePath) {
        STBImage.stbi_set_flip_vertically_on_load(true);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer width = stack.mallocInt(1);
            final IntBuffer height = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);
            final FloatBuffer pixels = STBImage.stbi_loadf(filePath, width, height, channels, 0);
            if (Objects.isNull(pixels)) {
                throw new EngineException("Can load HDR environment map");
            }
            final TextureParameters parameters = new TextureParameters().format(Format.RGB16F)
                    .sWrap(Wrap.CLAMP_TO_EDGE)
                    .tWrap(Wrap.CLAMP_TO_EDGE);
            final Texture envMap = new Texture(width.get(), height.get(), pixels, parameters);
            STBImage.stbi_image_free(pixels);
            return envMap;
        }
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.vertexBuffer.destroy();
        this.indexBuffer.destroy();
        this.environmentMap.destroy();
    }

    public VertexBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    public IndexBuffer getIndexBuffer() {
        return this.indexBuffer;
    }

    public Texture getEnvironmentMap() {
        return this.environmentMap;
    }

}
