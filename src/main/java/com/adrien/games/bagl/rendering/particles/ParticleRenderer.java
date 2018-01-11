package com.adrien.games.bagl.rendering.particles;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.texture.Texture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
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
    private static final int FLOAT_SIZE_IN_BYTES = Float.SIZE / 8;
    private static final int ELEMENTS_PER_VERTEX = 8;
    private static final int VERTEX_STRIDE = ELEMENTS_PER_VERTEX * FLOAT_SIZE_IN_BYTES;
    private static final int POSITION_INDEX = 0;
    private static final int POSITION_OFFSET = 0;
    private static final int ELEMENTS_PER_POSITION = 3;
    private static final int COLOR_INDEX = 1;
    private static final int COLOR_OFFSET = 3 * FLOAT_SIZE_IN_BYTES;
    private static final int ELEMENTS_PER_COLOR = 4;
    private static final int SIZE_INDEX = 2;
    private static final int SIZE_OFFSET = 7 * FLOAT_SIZE_IN_BYTES;
    private static final int ELEMENTS_PER_SIZE = 1;

    private static Logger log = LogManager.getLogger(ParticleRenderer.class);

    private final Shader shader;
    private final FloatBuffer vertices;
    private final int vaoId;
    private final int vboId;
    private final List<Particle> particlesToRender;
    private final Time timer;

    /**
     * Construct the particle renderer
     */
    public ParticleRenderer() {
        this.shader = new Shader()
                .addVertexShader("/particles/particles.vert")
                .addFragmentShader("/particles/particles.frag")
                .addGeometryShader("/particles/particles.geom")
                .compile();

        this.vertices = MemoryUtil.memAllocFloat(ParticleEmitter.MAX_PARTICLE_COUNT * ELEMENTS_PER_VERTEX);
        this.vaoId = GL30.glGenVertexArrays();
        this.vboId = GL15.glGenBuffers();
        GL30.glBindVertexArray(this.vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);

        GL20.glEnableVertexAttribArray(POSITION_INDEX);
        GL20.glVertexAttribPointer(POSITION_INDEX, ELEMENTS_PER_POSITION, GL11.GL_FLOAT, false, VERTEX_STRIDE, POSITION_OFFSET);

        GL20.glEnableVertexAttribArray(COLOR_INDEX);
        GL20.glVertexAttribPointer(COLOR_INDEX, ELEMENTS_PER_COLOR, GL11.GL_FLOAT, false, VERTEX_STRIDE, COLOR_OFFSET);

        GL20.glEnableVertexAttribArray(SIZE_INDEX);
        GL20.glVertexAttribPointer(SIZE_INDEX, ELEMENTS_PER_SIZE, GL11.GL_FLOAT, false, VERTEX_STRIDE, SIZE_OFFSET);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        this.particlesToRender = new ArrayList<>();
        this.timer = new Time();
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.shader.destroy();
        MemoryUtil.memFree(this.vertices);
        GL15.glDeleteBuffers(this.vboId);
        GL30.glDeleteVertexArrays(this.vaoId);
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
            this.vertices.put(index, p.getPosition().getX());
            this.vertices.put(index + 1, p.getPosition().getY());
            this.vertices.put(index + 2, p.getPosition().getZ());
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
        GL30.glBindVertexArray(this.vaoId);

        this.timer.update();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboId);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, this.vertices);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        log.debug("Copying data to gpu : {}", this.timer.getElapsedTime());

        Engine.setBlendMode(emitter.getBlendMode());
        GL11.glDepthMask(false);

        this.timer.update();
        GL11.glDrawArrays(GL11.GL_POINTS, 0, particleToRender);
        log.debug("Rendering {} particles : {}", particleToRender, this.timer.getElapsedTime());

        GL11.glDepthMask(true);
        Engine.setBlendMode(BlendMode.NONE);

        GL30.glBindVertexArray(0);
        Shader.unbind();
        Texture.unbind();
    }

    /**
     * This comparators is used to sort particles from the furthest aways from
     * the camera to the closest
     */
    private static class ParticleComparator implements Comparator<Particle> {

        private Camera camera;
        private final Vector3 v0 = new Vector3();
        private final Vector3 v1 = new Vector3();

        @Override
        public int compare(final Particle p0, final Particle p1) {
            Vector3.sub(p0.getPosition(), camera.getPosition(), v0);
            Vector3.sub(p1.getPosition(), camera.getPosition(), v1);
            final float dist0 = v0.squareLength();
            final float dist1 = v1.squareLength();
            return (int) (dist1 - dist0);
        }

        public void setCamera(final Camera camera) {
            this.camera = camera;
        }
    }
}
