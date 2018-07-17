package com.adrien.games.bagl.rendering.particles;

import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.VertexArray;
import com.adrien.games.bagl.rendering.vertex.VertexBuffer;
import com.adrien.games.bagl.rendering.vertex.VertexBufferParams;
import com.adrien.games.bagl.rendering.vertex.VertexElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Render particles using OpenGL geometry shaders
 *
 * @author adrien
 */
public class ParticleRenderer {

    private static final ParticleComparator COMPARATOR = new ParticleComparator();
    private static final int ELEMENTS_PER_VERTEX = 8;
    private static final int POSITION_INDEX = 0;
    private static final int ELEMENTS_PER_POSITION = 3;
    private static final int COLOR_INDEX = 1;
    private static final int ELEMENTS_PER_COLOR = 4;
    private static final int SIZE_INDEX = 2;
    private static final int ELEMENTS_PER_SIZE = 1;

    private static Logger log = LogManager.getLogger(ParticleRenderer.class);

    private final Shader shader;
    private final FloatBuffer vertices;
    private VertexBuffer vBuffer;
    private VertexArray vArray;
    private final List<Particle> particlesToRender;
    private final Time timer;

    /**
     * Construct the particle renderer
     */
    public ParticleRenderer() {
        this.shader = Shader.builder()
                .vertexPath("classpath:/shaders/particles/particles.vert")
                .fragmentPath("classpath:/shaders/particles/particles.frag")
                .geometryPath("classpath:/shaders/particles/particles.geom")
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
        this.timer = new Time();
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.shader.destroy();
        MemoryUtil.memFree(this.vertices);
        this.vBuffer.destroy();
        this.vArray.destroy();
    }

    /**
     * Render all particles owned by the passed in {@link ParticleEmitter} from
     * a {@link Camera} point of view
     *
     * @param emitter The emitter to render
     * @param camera  The camera
     */
    public void render(
            final ParticleEmitter emitter,
            final Camera camera,
            final List<DirectionalLight> directionalLights,
            final List<PointLight> pointLights,
            final List<SpotLight> spotLights
    ) {
        var particleToRender = 0;

        this.timer.update();
        this.particlesToRender.clear();
        Arrays.stream(emitter.getParticles()).filter(Particle::isAlive).forEach(this.particlesToRender::add);
        log.debug("Retrieving active particles: {}", this.timer.getElapsedTime());

        if (emitter.getBlendMode() != BlendMode.ADDITIVE) {
            this.timer.update();
            COMPARATOR.setCamera(camera);
            this.particlesToRender.sort(COMPARATOR);
            log.debug("Sorting particles: {}", this.timer.getElapsedTime());
        }

        this.timer.update();
        for (final var p : this.particlesToRender) {
            final var index = particleToRender * ELEMENTS_PER_VERTEX;
            this.vertices.put(index, p.getPosition().x());
            this.vertices.put(index + 1, p.getPosition().y());
            this.vertices.put(index + 2, p.getPosition().z());
            this.vertices.put(index + 3, p.getColor().getRed());
            this.vertices.put(index + 4, p.getColor().getGreen());
            this.vertices.put(index + 5, p.getColor().getBlue());
            this.vertices.put(index + 6, p.getColor().getAlpha());
            this.vertices.put(index + 7, p.getSize());
            particleToRender++;
        }
        log.debug("Copying data to cpu buffer : {}", this.timer.getElapsedTime());

        final AtomicBoolean hasTexture = new AtomicBoolean(false);
        emitter.getTexture().ifPresent(texture -> {
            hasTexture.set(true);
            texture.bind();
        });

        this.shader.bind();
        this.shader.setUniform("hasTexture", hasTexture.get());
        this.shader.setUniform("uCamera.view", camera.getView());
        this.shader.setUniform("uCamera.viewProj", camera.getViewProj());

        this.shader.setUniform("uLights.directionalCount", directionalLights.size());
        for (int i = 0; i < directionalLights.size(); i++) {
            this.setDirectionalLight(this.shader, i, directionalLights.get(i));
        }
        this.shader.setUniform("uLights.pointCount", pointLights.size());
        for (int i = 0; i < pointLights.size(); i++) {
            this.setPointLight(this.shader, i, pointLights.get(i));
        }
        this.shader.setUniform("uLights.spotCount", spotLights.size());
        for (int i = 0; i < spotLights.size(); i++) {
            this.setSpotLight(this.shader, i, spotLights.get(i));
        }

        this.timer.update();
        this.vBuffer.bind();
        this.vBuffer.update(this.vertices);
        this.vBuffer.unbind();
        log.debug("Copying data to gpu : {}", this.timer.getElapsedTime());

        this.vArray.bind();

        Engine.setBlendMode(emitter.getBlendMode());
        GL11.glDepthMask(false);

        this.timer.update();
        GL11.glDrawArrays(GL11.GL_POINTS, 0, particleToRender);
        log.debug("Rendering {} particles : {}", particleToRender, this.timer.getElapsedTime());

        GL11.glDepthMask(true);
        Engine.setBlendMode(BlendMode.NONE);

        this.vArray.unbind();
        Shader.unbind();
        Texture.unbind();
    }

    private void setDirectionalLight(final Shader shader, final int index, final DirectionalLight light) {
        shader.setUniform("uLights.directionals[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.directionals[" + index + "].base.color", light.getColor())
                .setUniform("uLights.directionals[" + index + "].direction", light.getDirection());
    }

    private void setPointLight(Shader shader, int index, PointLight light) {
        shader.setUniform("uLights.points[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.points[" + index + "].base.color", light.getColor())
                .setUniform("uLights.points[" + index + "].position", light.getPosition())
                .setUniform("uLights.points[" + index + "].radius", light.getRadius());
    }

    private void setSpotLight(Shader shader, int index, SpotLight light) {
        shader.setUniform("uLights.spots[" + index + "].point.base.intensity", light.getIntensity())
                .setUniform("uLights.spots[" + index + "].point.base.color", light.getColor())
                .setUniform("uLights.spots[" + index + "].point.position", light.getPosition())
                .setUniform("uLights.spots[" + index + "].point.radius", light.getRadius())
                .setUniform("uLights.spots[" + index + "].direction", light.getDirection())
                .setUniform("uLights.spots[" + index + "].cutOff", light.getCutOff())
                .setUniform("uLights.spots[" + index + "].outerCutOff", light.getOuterCutOff());
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
            p0.getPosition().sub(this.camera.getPosition(), this.v0);
            p1.getPosition().sub(this.camera.getPosition(), this.v1);
            final var dist0 = this.v0.lengthSquared();
            final var dist1 = this.v1.lengthSquared();
            return Float.compare(dist1, dist0);
        }

        public void setCamera(final Camera camera) {
            this.camera = camera;
        }
    }
}
