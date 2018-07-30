package com.adrien.games.bagl.engine.rendering.environment;

import com.adrien.games.bagl.core.exception.EngineException;
import com.adrien.games.bagl.core.io.ResourcePath;
import com.adrien.games.bagl.core.math.MathUtils;
import com.adrien.games.bagl.engine.Configuration;
import com.adrien.games.bagl.engine.camera.Camera;
import com.adrien.games.bagl.engine.rendering.model.Mesh;
import com.adrien.games.bagl.engine.rendering.model.MeshFactory;
import com.adrien.games.bagl.opengl.FrameBuffer;
import com.adrien.games.bagl.opengl.FrameBufferParameters;
import com.adrien.games.bagl.opengl.shader.Shader;
import com.adrien.games.bagl.opengl.texture.*;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author adrien
 */
public class EnvironmentMapGenerator {

    private final static int ENVIRONMENT_MAP_RESOLUTION = 1024;
    private final static int IRRADIANCE_MAP_RESOLUTION = 64;
    private final static int PRE_FILTERED_MAP_RESOLUTION = 256;
    private final static float FIELD_OF_VIEW = MathUtils.toRadians(90);
    private final static float ASPECT_RATIO = 1f;
    private final static float NEAR_PLANE = 0.1f;
    private final static float FAR_PLANE = 10f;

    private final Mesh cubeMapMesh;

    private FrameBuffer environmentFrameBuffer;
    private FrameBuffer irradianceFrameBuffer;
    private FrameBuffer preFilteredMapFrameBuffer;

    private Shader environmentSphericalShader;
    private Shader irradianceShader;
    private Shader preFilteredMapShader;

    private Camera[] cameras;

    public EnvironmentMapGenerator() {
        this.cubeMapMesh = MeshFactory.createCubeMapMesh();

        final var frameBufferParameters = FrameBufferParameters.builder().hasDepthStencil(false).build();
        this.environmentFrameBuffer = new FrameBuffer(ENVIRONMENT_MAP_RESOLUTION, ENVIRONMENT_MAP_RESOLUTION, frameBufferParameters);
        this.irradianceFrameBuffer = new FrameBuffer(IRRADIANCE_MAP_RESOLUTION, IRRADIANCE_MAP_RESOLUTION, frameBufferParameters);
        this.preFilteredMapFrameBuffer = new FrameBuffer(PRE_FILTERED_MAP_RESOLUTION, PRE_FILTERED_MAP_RESOLUTION, frameBufferParameters);

        this.environmentSphericalShader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/environment/environment.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/environment/environment_spherical_sample.frag"))
                .build();
        this.irradianceShader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/environment/environment.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/environment/irradiance.frag"))
                .build();
        this.preFilteredMapShader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/environment/environment.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/environment/pre_filtered_map.frag"))
                .build();

        this.cameras = this.initCameras();
    }

    /**
     * Destroy resources
     */
    public void destroy() {
        this.cubeMapMesh.destroy();
        this.environmentSphericalShader.destroy();
        this.irradianceShader.destroy();
        this.preFilteredMapShader.destroy();
        this.environmentFrameBuffer.destroy();
        this.irradianceFrameBuffer.destroy();
        this.preFilteredMapFrameBuffer.destroy();
    }

    /**
     * Generate an environment map from a HDR equirectangular image
     *
     * @param path The path of the HDR image file
     * @return An {@link Cubemap}
     */
    public Cubemap generateEnvironmentMap(final ResourcePath path) {
        final var params = TextureParameters.builder()
                .sWrap(Wrap.CLAMP_TO_EDGE)
                .tWrap(Wrap.CLAMP_TO_EDGE);
        final var equirectangularMap = Texture.fromFile(path, true, params);

        final var cubemapParams = TextureParameters.builder()
                .format(Format.RGB16F)
                .minFilter(Filter.MIPMAP_LINEAR_LINEAR)
                .build();
        final var cubemap = new Cubemap(ENVIRONMENT_MAP_RESOLUTION, ENVIRONMENT_MAP_RESOLUTION, cubemapParams);

        equirectangularMap.bind();
        this.environmentSphericalShader.bind();
        this.renderToCubemap(cubemap, 0, this.environmentSphericalShader, this.environmentFrameBuffer);
        Shader.unbind();
        Texture.unbind();

        cubemap.bind();
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        Cubemap.unbind();

        equirectangularMap.destroy();
        return cubemap;
    }

    /**
     * Generate the irradiance map from a cubemap
     *
     * @param environmentMap The cubemap from which to compute the irradiance map
     * @return An {@link Cubemap}
     */
    public Cubemap generateIrradianceMap(final Cubemap environmentMap) {
        final var cubemap = new Cubemap(IRRADIANCE_MAP_RESOLUTION, IRRADIANCE_MAP_RESOLUTION,
                TextureParameters.builder().format(Format.RGB16F).build());
        environmentMap.bind();
        this.irradianceShader.bind();
        this.renderToCubemap(cubemap, 0, this.irradianceShader, this.irradianceFrameBuffer);
        Cubemap.unbind();
        Shader.unbind();
        return cubemap;
    }

    /**
     * Generate a pre-filtered map from another environment map
     *
     * @param environmentMap The environment map from which to generate the pre-filtered map
     * @return An {@link Cubemap}
     */
    public Cubemap generatePreFilteredMap(final Cubemap environmentMap) {
        final var cubemapParams = TextureParameters.builder()
                .format(Format.RGB16F)
                .mipmaps(true)
                .minFilter(Filter.MIPMAP_LINEAR_LINEAR)
                .build();
        final var cubemap = new Cubemap(PRE_FILTERED_MAP_RESOLUTION, PRE_FILTERED_MAP_RESOLUTION, cubemapParams);

        this.preFilteredMapShader.bind();
        environmentMap.bind();
        final var maxLod = 5;
        for (int lod = 0; lod < maxLod; lod++) {
            final var roughness = (float) lod / (float) (maxLod - 1);
            this.preFilteredMapShader.setUniform("roughness", roughness);
            this.renderToCubemap(cubemap, lod, this.preFilteredMapShader, this.preFilteredMapFrameBuffer);
        }
        Cubemap.unbind();
        Shader.unbind();

        return cubemap;
    }

    /**
     * Render a scene in a cubemap
     *
     * @param target      The target cubemap in which to render
     * @param mipLevel    The level of mipmap in which to render
     * @param shader      The shader to use
     * @param frameBuffer The frame buffer to use
     */
    private void renderToCubemap(final Cubemap target, final int mipLevel, final Shader shader, final FrameBuffer frameBuffer) {
        frameBuffer.bind();
        frameBuffer.enableColorOutputs(0);

        final var iBuffer = this.cubeMapMesh.getIndexBuffer().orElseThrow(() -> new EngineException("Cube map mesh should have an index"));
        this.cubeMapMesh.getVertexArray().bind();
        iBuffer.bind();

        final var mipFactor = 1f / (float) Math.pow(2, mipLevel);
        glViewport(0, 0, (int) (target.getWidth() * mipFactor), (int) (target.getHeight() * mipFactor));
        for (int i = 0; i < 6; i++) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    target.getHandle(), mipLevel);

            shader.setUniform("viewProj", this.cameras[i].getViewProjAtOrigin());

            glDrawElements(GL_TRIANGLES, iBuffer.getSize(), iBuffer.getDataType().getGlCode(), 0);
        }
        glViewport(0, 0, Configuration.getInstance().getXResolution(), Configuration.getInstance().getYResolution());

        iBuffer.unbind();
        this.cubeMapMesh.getVertexArray().unbind();
        frameBuffer.unbind();
    }

    private Camera[] initCameras() {
        final var cameras = new Camera[6];
        cameras[0] = this.createCamera(new Vector3f(1, 0, 0), new Vector3f(0, -1, 0)); // right
        cameras[1] = this.createCamera(new Vector3f(-1, 0, 0), new Vector3f(0, -1, 0)); // left
        cameras[2] = this.createCamera(new Vector3f(0, 1, 0), new Vector3f(0, 0, 1)); // top
        cameras[3] = this.createCamera(new Vector3f(0, -1, 0), new Vector3f(0, 0, -1)); // bottom
        cameras[4] = this.createCamera(new Vector3f(0, 0, 1), new Vector3f(0, -1, 0)); // back
        cameras[5] = this.createCamera(new Vector3f(0, 0, -1), new Vector3f(0, -1, 0)); // front
        return cameras;
    }

    private Camera createCamera(final Vector3f direction, final Vector3f up) {
        return new Camera(new Vector3f(0, 0, 0), direction, up, FIELD_OF_VIEW, ASPECT_RATIO, NEAR_PLANE, FAR_PLANE);
    }

}
