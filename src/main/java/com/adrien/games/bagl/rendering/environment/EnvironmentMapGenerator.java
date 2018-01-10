package com.adrien.games.bagl.rendering.environment;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.FrameBufferParameters;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.texture.*;
import com.adrien.games.bagl.utils.HDRImage;
import com.adrien.games.bagl.utils.ImageUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author adrien
 */
public class EnvironmentMapGenerator {

    private final static int ENVIRONMENT_MAP_RESOLUTION = 1024;
    private final static int IRRADIANCE_MAP_RESOLUTION = 64;
    private final static int PRE_FILTERED_MAP_RESOLUTION = 256;
    private final static float FIELD_OF_VIEW = (float) Math.toRadians(90);
    private final static float ASPECT_RATIO = 1f;
    private final static float NEAR_PLANE = 0.1f;
    private final static float FAR_PLANE = 10f;

    private final static byte SKYBOX_POSITIVE_HALF_SIZE = (byte) 1;
    private final static byte SKYBOX_NEGATIVE_HALF_SIZE = (byte) -1;

    private final int vboId;
    private final int vaoId;
    private final int iboId;

    private FrameBuffer environmentFrameBuffer;
    private FrameBuffer irradianceFrameBuffer;
    private FrameBuffer preFilteredMapFrameBuffer;

    private Shader environmentSphericalShader;
    private Shader irradianceShader;
    private Shader preFilteredMapShader;

    private Camera[] cameras;

    public EnvironmentMapGenerator() {
        this.vaoId = glGenVertexArrays();
        this.vboId = glGenBuffers();
        this.generateVertexBuffer();
        this.iboId = this.generateIndexBuffer();

        final FrameBufferParameters frameBufferParameters = new FrameBufferParameters().hasDepth(false);
        this.environmentFrameBuffer = new FrameBuffer(ENVIRONMENT_MAP_RESOLUTION, ENVIRONMENT_MAP_RESOLUTION, frameBufferParameters);
        this.irradianceFrameBuffer = new FrameBuffer(IRRADIANCE_MAP_RESOLUTION, IRRADIANCE_MAP_RESOLUTION, frameBufferParameters);
        this.preFilteredMapFrameBuffer = new FrameBuffer(PRE_FILTERED_MAP_RESOLUTION, PRE_FILTERED_MAP_RESOLUTION, frameBufferParameters);

        this.environmentSphericalShader = new Shader()
                .addVertexShader("/environment/environment.vert")
                .addFragmentShader("/environment/environment_spherical_sample.frag")
                .compile();
        this.irradianceShader = new Shader()
                .addVertexShader("/environment/environment.vert")
                .addFragmentShader("/environment/irradiance.frag")
                .compile();
        this.preFilteredMapShader = new Shader()
                .addVertexShader("/environment/environment.vert")
                .addFragmentShader("/environment/pre_filtered_map.frag")
                .compile();

        this.cameras = this.initCameras();
    }

    /**
     * Destroy resources
     */
    public void destroy() {
        glDeleteBuffers(this.iboId);
        glDeleteBuffers(this.vboId);
        glDeleteVertexArrays(this.vaoId);
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
     * @param filePath The path of the HDR image file
     * @return An {@link Cubemap}
     */
    public Cubemap generateEnvironmentMap(final String filePath) {
        final HDRImage hdrImage = ImageUtils.loadHDRImage(filePath);
        final Texture equirectangularMap = new Texture(hdrImage.getWidth(), hdrImage.getHeight(), hdrImage.getData(),
                new TextureParameters().format(Format.RGB16F).sWrap(Wrap.CLAMP_TO_EDGE).tWrap(Wrap.CLAMP_TO_EDGE));
        final Cubemap cubemap = new Cubemap(ENVIRONMENT_MAP_RESOLUTION, ENVIRONMENT_MAP_RESOLUTION,
                new TextureParameters().format(Format.RGB16F).minFilter(Filter.MIPMAP_LINEAR_LINEAR));

        equirectangularMap.bind();
        this.environmentSphericalShader.bind();
        this.renderToCubemap(cubemap, 0, this.environmentSphericalShader, this.environmentFrameBuffer);
        Shader.unbind();
        Texture.unbind();

        cubemap.bind();
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        Cubemap.unbind();

        equirectangularMap.destroy();
        ImageUtils.free(hdrImage);
        return cubemap;
    }

    /**
     * Generate the irradiance map from a cubemap
     *
     * @param environmentMap The cubemap from which to compute the irradiance map
     * @return An {@link Cubemap}
     */
    public Cubemap generateIrradianceMap(final Cubemap environmentMap) {
        final Cubemap cubemap = new Cubemap(IRRADIANCE_MAP_RESOLUTION, IRRADIANCE_MAP_RESOLUTION,
                new TextureParameters().format(Format.RGB16F));
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
        final Cubemap cubemap = new Cubemap(PRE_FILTERED_MAP_RESOLUTION, PRE_FILTERED_MAP_RESOLUTION, new TextureParameters()
                .format(Format.RGB16F).mipmaps(true).minFilter(Filter.MIPMAP_LINEAR_LINEAR));

        this.preFilteredMapShader.bind();
        environmentMap.bind();
        final int maxLod = 5;
        for (int lod = 0; lod < maxLod; lod++) {
            final float roughness = (float) lod / (float) (maxLod - 1);
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

        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.iboId);

        final float mipFactor = 1f / (float) Math.pow(2, mipLevel);
        glViewport(0, 0, (int) (target.getWidth() * mipFactor), (int) (target.getHeight() * mipFactor));
        for (int i = 0; i < 6; i++) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    target.getHandle(), mipLevel);

            shader.setUniform("viewProj", this.cameras[i].getViewProjAtOrigin());

            glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, 0);
        }
        glViewport(0, 0, Configuration.getInstance().getXResolution(), Configuration.getInstance().getYResolution());

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        frameBuffer.unbind();
    }

    private int generateIndexBuffer() {
        final int iboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer indices = stack.bytes(
                    (byte) 1, (byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 2,
                    (byte) 5, (byte) 1, (byte) 7, (byte) 7, (byte) 1, (byte) 3,
                    (byte) 4, (byte) 5, (byte) 6, (byte) 6, (byte) 5, (byte) 7,
                    (byte) 0, (byte) 4, (byte) 2, (byte) 2, (byte) 4, (byte) 6,
                    (byte) 6, (byte) 7, (byte) 2, (byte) 2, (byte) 7, (byte) 3,
                    (byte) 0, (byte) 1, (byte) 4, (byte) 4, (byte) 1, (byte) 5
            );
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        return iboId;
    }

    private void generateVertexBuffer() {
        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer vertices = stack.bytes(
                    SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE,
                    SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE,
                    SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE,
                    SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE,
                    SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE,
                    SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE,
                    SKYBOX_NEGATIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE,
                    SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_POSITIVE_HALF_SIZE, SKYBOX_NEGATIVE_HALF_SIZE);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        }
        glEnableVertexAttribArray(0);
        glVertexAttribIPointer(0, 3, GL_BYTE, 3, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
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
