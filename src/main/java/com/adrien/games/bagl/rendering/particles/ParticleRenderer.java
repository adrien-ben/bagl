package com.adrien.games.bagl.rendering.particles;

import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Shader;
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
import java.util.*;

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
                .vertexPath("/particles/particles.vert")
                .fragmentPath("/particles/particles.frag")
                .geometryPath("/particles/particles.geom")
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
    public void render(final ParticleEmitter emitter, final Camera camera) {
        int particleToRender = 0;

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
        for (final Particle p : this.particlesToRender) {
            final int index = particleToRender * ELEMENTS_PER_VERTEX;
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

        boolean hasTexture;
        final Optional<Texture> texture = emitter.getTexture();
        if (hasTexture = texture.isPresent()) {
            texture.get().bind();
        }

        this.shader.bind();
        this.shader.setUniform("hasTexture", hasTexture);
        this.shader.setUniform("camera.view", camera.getView());
        this.shader.setUniform("camera.viewProj", camera.getViewProj());

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
            final float dist0 = this.v0.lengthSquared();
            final float dist1 = this.v1.lengthSquared();
            return (int) (dist1 - dist0);
        }

        public void setCamera(final Camera camera) {
            this.camera = camera;
        }
    }
}
