package com.adrien.games.bagl.rendering.particles;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexDescription;
import com.adrien.games.bagl.rendering.vertex.VertexElement;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Renders particles using OpenGL geometry shaders.
 */
public class ParticleRenderer {

    private static final ParticleComparator COMPARATOR = new ParticleComparator();

    private final Shader shader;
    private final VertexBuffer vBuffer;
    private final ParticleVertex[] vertices;
    private final List<Particle> particlesToRender;
    private final Time timer;

    public ParticleRenderer() {
        this.shader = new Shader()
                .addVertexShader("/particles/particles.vert")
                .addFragmentShader("/particles/particles.frag")
                .addGeometryShader("/particles/particles.geom")
                .compile();

        this.vertices = new ParticleVertex[ParticleEmitter.MAX_PARTICLE_COUNT];
        for (int i = 0; i < ParticleEmitter.MAX_PARTICLE_COUNT; i++) {
            this.vertices[i] = new ParticleVertex(new Vector3(), Color.WHITE, 1f);
        }
        this.vBuffer = new VertexBuffer(ParticleVertex.getDescription(), BufferUsage.STREAM_DRAW, this.vertices);
        this.particlesToRender = new ArrayList<>();
        this.timer = new Time();
    }

    /**
     * Renders all particles owned by the passed in {@link ParticleEmitter} from
     * a {@link Camera} point of view.
     *
     * @param emitter The emitter to render.
     * @param camera  The camera.
     */
    public void render(final ParticleEmitter emitter, final Camera camera) {
        int particleToRender = 0;

        this.timer.update();
        this.particlesToRender.clear();
        Arrays.stream(emitter.getParticles()).filter(Particle::isAlive).forEach(this.particlesToRender::add);
        System.out.println("Retrieving active particles: " + this.timer.getElapsedTime());

        if (emitter.getBlendMode() != BlendMode.ADDITIVE) {
            this.timer.update();
            COMPARATOR.setCamera(camera);
            this.particlesToRender.sort(COMPARATOR);
            System.out.println("Sorting particles: " + this.timer.getElapsedTime());
        }

        this.timer.update();
        for (Particle p : this.particlesToRender) {
            final ParticleVertex particleVertex = this.vertices[particleToRender];
            particleVertex.position = p.getPosition();
            particleVertex.color = p.getColor();
            particleVertex.size = p.getSize();
            particleToRender++;
        }
        System.out.println("Copying data to cpu buffer : " + this.timer.getElapsedTime());

        boolean hasTexture;
        final Optional<Texture> texture = emitter.getTexture();
        if (hasTexture = texture.isPresent()) {
            texture.get().bind();
        }

        this.shader.bind();
        this.shader.setUniform("hasTexture", hasTexture);
        this.shader.setUniform("camera.view", camera.getView());
        this.shader.setUniform("camera.viewProj", camera.getViewProj());
        this.vBuffer.bind();

        this.timer.update();
        this.vBuffer.setData(this.vertices, particleToRender);
        System.out.println("Copying data to gpu : " + this.timer.getElapsedTime());

        Engine.setBlendMode(emitter.getBlendMode());
        GL11.glDepthMask(false);

        this.timer.update();
        GL11.glDrawArrays(GL11.GL_POINTS, 0, particleToRender);
        System.out.println("Rendering " + particleToRender + " particles : " + this.timer.getElapsedTime());

        GL11.glDepthMask(true);
        Engine.setBlendMode(BlendMode.NONE);

        VertexBuffer.unbind();
        Shader.unbind();
        Texture.unbind();
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.shader.destroy();
        this.vBuffer.destroy();
    }

    /**
     * Particle vertex class.
     */
    private static class ParticleVertex implements Vertex {

        private Vector3 position;
        private Color color;
        private float size;

        ParticleVertex(final Vector3 position, final Color color, final float size) {
            this.position = position;
            this.color = color;
            this.size = size;
        }

        @Override
        public float[] getData() {
            return new float[]{this.position.getX(), this.position.getY(), this.position.getZ(), this.color.getRed(),
                    this.color.getGreen(), this.color.getBlue(), this.color.getAlpha(), this.size};
        }

        public static VertexDescription getDescription() {
            return new VertexDescription(new VertexElement[]
                    {
                            new VertexElement(0, 3, 0),
                            new VertexElement(1, 4, 3),
                            new VertexElement(2, 1, 7)
                    });
        }

    }

    /**
     * This comparators is used to sort particles from the furthest aways from
     * the camera to the closest.
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
