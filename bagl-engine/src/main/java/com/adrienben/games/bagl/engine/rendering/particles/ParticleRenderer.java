package com.adrienben.games.bagl.engine.rendering.particles;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.rendering.light.SpotLight;
import com.adrienben.games.bagl.engine.rendering.renderer.Renderer;
import com.adrienben.games.bagl.opengl.BlendMode;
import com.adrienben.games.bagl.opengl.BufferUsage;
import com.adrienben.games.bagl.opengl.OpenGL;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Texture;
import com.adrienben.games.bagl.opengl.vertex.VertexArray;
import com.adrienben.games.bagl.opengl.vertex.VertexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexBufferParams;
import com.adrienben.games.bagl.opengl.vertex.VertexElement;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Render particles using OpenGL geometry shaders
 *
 * @author adrien
 */
public class ParticleRenderer implements Renderer<ParticleEmitter> {

    private static final ParticleComparator PARTICLE_COMPARATOR = new ParticleComparator();
    private static final int ELEMENTS_PER_VERTEX = 8;
    private static final int POSITION_INDEX = 0;
    private static final int ELEMENTS_PER_POSITION = 3;
    private static final int COLOR_INDEX = 1;
    private static final int ELEMENTS_PER_COLOR = 4;
    private static final int SIZE_INDEX = 2;
    private static final int ELEMENTS_PER_SIZE = 1;

    private final Shader shader;
    private final FloatBuffer vertices;
    private final VertexBuffer vBuffer;
    private final VertexArray vArray;
    private final List<Particle> particlesToRender;

    private Camera camera;
    private List<DirectionalLight> directionalLights;
    private List<PointLight> pointLights;
    private List<SpotLight> spotLights;

    /**
     * Construct the particle renderer
     */
    public ParticleRenderer() {
        this.shader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/particles/particles.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/particles/particles.frag"))
                .geometryPath(ResourcePath.get("classpath:/shaders/particles/particles.geom"))
                .build();

        this.vertices = MemoryUtil.memAllocFloat(ParticleEmitter.MAX_PARTICLE_COUNT * ELEMENTS_PER_VERTEX);
        this.vBuffer = new VertexBuffer(this.vertices, VertexBufferParams.builder()
                .usage(BufferUsage.STREAM_DRAW)
                .element(new VertexElement(POSITION_INDEX, ELEMENTS_PER_POSITION))
                .element(new VertexElement(COLOR_INDEX, ELEMENTS_PER_COLOR))
                .element(new VertexElement(SIZE_INDEX, ELEMENTS_PER_SIZE))
                .build());

        this.vArray = new VertexArray();
        this.vArray.bind();
        this.vArray.attachVertexBuffer(this.vBuffer);
        this.vArray.unbind();

        this.particlesToRender = new ArrayList<>();
    }

    /**
     * Release resources
     */
    public void destroy() {
        shader.destroy();
        MemoryUtil.memFree(vertices);
        vBuffer.destroy();
        vArray.destroy();
    }

    /**
     * Render all particles owned by the passed in {@link ParticleEmitter} from
     * a {@link Camera} point of view
     *
     * @param emitter The emitter to render
     */
    @Override
    public void render(final ParticleEmitter emitter) {
        checkRenderPreConditions();

        bufferParticlesToRender(emitter);
        sortParticlesIfEmitterHasNotAdditiveBlending(emitter);
        generateParticleVertices();

        final AtomicBoolean hasTexture = new AtomicBoolean(false);
        emitter.getTexture().ifPresent(texture -> {
            hasTexture.set(true);
            texture.bind();
        });

        shader.bind();
        shader.setUniform("hasTexture", hasTexture.get());
        setUpCameraShaderUniforms();
        setUpDirectionalLightsShaderUniforms();
        setUpPointLightsShaderUniforms();
        setUpSpotLightsShaderUniforms();

        vBuffer.bind();
        vBuffer.update(vertices);
        vBuffer.unbind();

        vArray.bind();

        OpenGL.setBlendMode(emitter.getBlendMode());
        GL11.glDepthMask(false);
        GL11.glDrawArrays(GL11.GL_POINTS, 0, particlesToRender.size());
        GL11.glDepthMask(true);
        OpenGL.setBlendMode(BlendMode.NONE);

        vArray.unbind();
        Shader.unbind();
        Texture.unbind();
    }

    private void bufferParticlesToRender(ParticleEmitter emitter) {
        particlesToRender.clear();
        Arrays.stream(emitter.getParticles()).filter(Particle::isAlive).forEach(particlesToRender::add);
    }

    private void checkRenderPreConditions() {
        if (Objects.isNull(camera)) {
            throw new EngineException("You need to set a camera before rendering particles.");
        }
    }

    private void sortParticlesIfEmitterHasNotAdditiveBlending(final ParticleEmitter emitter) {
        if (emitter.getBlendMode() != BlendMode.ADDITIVE) {
            PARTICLE_COMPARATOR.setCamera(camera);
            particlesToRender.sort(PARTICLE_COMPARATOR);
        }
    }

    private void generateParticleVertices() {
        var index = 0;
        for (final var p : particlesToRender) {
            vertices.put(index++, p.getPosition().x());
            vertices.put(index++, p.getPosition().y());
            vertices.put(index++, p.getPosition().z());
            vertices.put(index++, p.getColor().getRed());
            vertices.put(index++, p.getColor().getGreen());
            vertices.put(index++, p.getColor().getBlue());
            vertices.put(index++, p.getColor().getAlpha());
            vertices.put(index++, p.getSize());
        }
    }

    private void setUpCameraShaderUniforms() {
        shader.setUniform("uCamera.view", camera.getView());
        shader.setUniform("uCamera.viewProj", camera.getViewProj());
    }

    private void setUpDirectionalLightsShaderUniforms() {
        if (CollectionUtils.isNotEmpty(directionalLights)) {
            shader.setUniform("uLights.directionalCount", directionalLights.size());
            for (int i = 0; i < directionalLights.size(); i++) {
                setUpDirectionalLightShaderUniforms(i, directionalLights.get(i));
            }
        } else {
            shader.setUniform("uLights.directionalCount", 0);
        }
    }

    private void setUpPointLightsShaderUniforms() {
        if (CollectionUtils.isNotEmpty(pointLights)) {
            shader.setUniform("uLights.pointCount", pointLights.size());
            for (int i = 0; i < pointLights.size(); i++) {
                setUpPointLightShaderUniforms(i, pointLights.get(i));
            }
        } else {
            shader.setUniform("uLights.pointCount", 0);
        }
    }

    private void setUpSpotLightsShaderUniforms() {
        if (CollectionUtils.isNotEmpty(spotLights)) {
            shader.setUniform("uLights.spotCount", spotLights.size());
            for (int i = 0; i < spotLights.size(); i++) {
                setUpSpotLightShaderUniforms(i, spotLights.get(i));
            }
        } else {
            shader.setUniform("uLights.spotCount", 0);
        }
    }

    private void setUpDirectionalLightShaderUniforms(final int index, final DirectionalLight light) {
        shader.setUniform("uLights.directionals[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.directionals[" + index + "].base.color", light.getColor())
                .setUniform("uLights.directionals[" + index + "].direction", light.getDirection());
    }

    private void setUpPointLightShaderUniforms(final int index, final PointLight light) {
        shader.setUniform("uLights.points[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.points[" + index + "].base.color", light.getColor())
                .setUniform("uLights.points[" + index + "].position", light.getPosition())
                .setUniform("uLights.points[" + index + "].radius", light.getRadius());
    }

    private void setUpSpotLightShaderUniforms(final int index, final SpotLight light) {
        shader.setUniform("uLights.spots[" + index + "].point.base.intensity", light.getIntensity())
                .setUniform("uLights.spots[" + index + "].point.base.color", light.getColor())
                .setUniform("uLights.spots[" + index + "].point.position", light.getPosition())
                .setUniform("uLights.spots[" + index + "].point.radius", light.getRadius())
                .setUniform("uLights.spots[" + index + "].direction", light.getDirection())
                .setUniform("uLights.spots[" + index + "].cutOff", light.getCutOff())
                .setUniform("uLights.spots[" + index + "].outerCutOff", light.getOuterCutOff());
    }

    public void setCamera(final Camera camera) {
        this.camera = camera;
    }

    public void setDirectionalLights(final List<DirectionalLight> directionalLights) {
        this.directionalLights = directionalLights;
    }

    public void setPointLights(final List<PointLight> pointLights) {
        this.pointLights = pointLights;
    }

    public void setSpotLights(final List<SpotLight> spotLights) {
        this.spotLights = spotLights;
    }

    /**
     * This comparators is used to sort particles from the furthest aways from
     * the camera to the closest
     */
    private static class ParticleComparator implements Comparator<Particle> {

        private Camera camera;
        private final Vector3f v0 = new Vector3f();
        private final Vector3f v1 = new Vector3f();

        @Override
        public int compare(final Particle p0, final Particle p1) {
            p0.getPosition().sub(camera.getPosition(), v0);
            p1.getPosition().sub(camera.getPosition(), v1);
            final var dist0 = v0.lengthSquared();
            final var dist1 = v1.lengthSquared();
            return Float.compare(dist1, dist0);
        }

        public void setCamera(final Camera camera) {
            this.camera = camera;
        }
    }
}
