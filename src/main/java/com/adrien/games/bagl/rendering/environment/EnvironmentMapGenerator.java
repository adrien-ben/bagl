package com.adrien.games.bagl.rendering.environment;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.*;
import com.adrien.games.bagl.rendering.texture.*;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPosition;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * @author adrien
 */
public class EnvironmentMapGenerator {

    private final static int ENVIRONMENT_MAP_RESOLUTION = 1024;
    private final static int CONVOLUTION_RESOLUTION = 64;
    private final static float SKYBOX_HALF_SIZE = 0.5f;
    private final static float FIELD_OF_VIEW = (float) Math.toRadians(90);
    private final static float ASPECT_RATIO = 1f;
    private final static float NEAR_PLANE = 0.1f;
    private final static float FAR_PLANE = 10f;

    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;
    private Shader environmentSphericalShader;
    private Shader convolutionShader;
    private Camera[] cameras;

    public EnvironmentMapGenerator() {
        this.vertexBuffer = this.initVertices();
        this.indexBuffer = this.initIndices();
        this.environmentSphericalShader = new Shader().addVertexShader("/environment/environment.vert")
                .addFragmentShader("/environment/environment_spherical_sample.frag").compile();
        this.convolutionShader = new Shader().addVertexShader("/environment/environment.vert").addFragmentShader("/environment/convolution.frag")
                .compile();
        this.cameras = this.initCameras();
    }

    /**
     * Destroy resources
     */
    public void destroy() {
        this.vertexBuffer.destroy();
        this.indexBuffer.destroy();
        this.environmentSphericalShader.destroy();
        this.convolutionShader.destroy();
    }

    /**
     * Generate an environment map from a HDR equirectangular image
     *
     * @param filePath The path of the HDR image file
     * @return An {@link EnvironmentMap}
     */
    public EnvironmentMap generate(final String filePath) {
        final Texture equirectangularMap = this.loadHDREnvMap(filePath);
        final Cubemap cubemap = this.renderToCubemap(ENVIRONMENT_MAP_RESOLUTION, Format.RGB16F, this.environmentSphericalShader,
                equirectangularMap::bind, Texture::unbind);
        equirectangularMap.destroy();
        return new EnvironmentMap(cubemap);
    }

    /**
     * Generate an environment map which is the convolution of another environment map
     *
     * @param environmentMap The environment map from which to generate the convolution
     * @return An {@link EnvironmentMap}
     */
    public EnvironmentMap generateConvolution(final EnvironmentMap environmentMap) {
        final Cubemap cubemap = this.renderToCubemap(CONVOLUTION_RESOLUTION, Format.RGB16F, this.convolutionShader,
                environmentMap.getCubemap()::bind, Cubemap::unbind);
        return new EnvironmentMap(cubemap);
    }

    /**
     * Render a scene in a cubemap
     *
     * @param cubemapResolution The resolution of the generated cubemap
     * @param cubemapFormat     The format of the generated cubemap
     * @param shader            The shader to use
     * @param preRender         The action(s) to perform before the rendering loop (binding textures, ...)
     * @param postRender        The action(s) to perform after the rendering loop (unbinding textures, ...)
     * @return A {@link Cubemap}
     */
    private Cubemap renderToCubemap(final int cubemapResolution, final Format cubemapFormat, final Shader shader,
                                    final Runnable preRender, final Runnable postRender) {

        final Cubemap cubemap = new Cubemap(cubemapResolution, cubemapResolution, new TextureParameters().format(cubemapFormat));
        final FrameBuffer frameBuffer = new FrameBuffer(cubemapResolution, cubemapResolution, new FrameBufferParameters().hasDepth(false));
        frameBuffer.bind();
        frameBuffer.enableColorOutputs(0);

        this.vertexBuffer.bind();
        this.indexBuffer.bind();
        shader.bind();
        preRender.run();

        glViewport(0, 0, cubemapResolution, cubemapResolution);
        for (int i = 0; i < 6; i++) {
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    cubemap.getHandle(), 0);
            frameBuffer.clear();

            shader.setUniform("viewProj", this.cameras[i].getViewProjAtOrigin());

            glDrawElements(GL11.GL_TRIANGLES, this.indexBuffer.getSize(), GL11.GL_UNSIGNED_INT, 0);
        }
        glViewport(0, 0, Configuration.getInstance().getXResolution(), Configuration.getInstance().getYResolution());

        postRender.run();
        Shader.unbind();
        IndexBuffer.unbind();
        VertexBuffer.unbind();
        frameBuffer.destroy();

        return cubemap;
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

    private Camera[] initCameras() {
        final Camera[] cameras = new Camera[6];
        cameras[0] = this.createCamera(new Vector3(1, 0, 0), new Vector3(0, -1, 0)); // right
        cameras[1] = this.createCamera(new Vector3(-1, 0, 0), new Vector3(0, -1, 0)); // left
        cameras[2] = this.createCamera(new Vector3(0, 1, 0), new Vector3(0, 0, 1)); // top
        cameras[3] = this.createCamera(new Vector3(0, -1, 0), new Vector3(0, 0, -1)); // bottom
        cameras[4] = this.createCamera(new Vector3(0, 0, 1), new Vector3(0, -1, 0)); // back
        cameras[5] = this.createCamera(new Vector3(0, 0, -1), new Vector3(0, -1, 0)); // front
        return cameras;
    }

    private Camera createCamera(final Vector3 direction, final Vector3 up) {
        return new Camera(new Vector3(0, 0, 0), direction, up, FIELD_OF_VIEW, ASPECT_RATIO, NEAR_PLANE, FAR_PLANE);
    }

}
