package com.adrien.games.bagl.rendering.particles;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.BlendMode;
import com.adrien.games.bagl.rendering.texture.Texture;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * <p>Particle emitter.
 * <p>A particle emitter is responsible to spawn particles at a given rate.
 * The number of particles spawn and spawn rate can be configured. A emitter
 * can have a {@link Texture} associated, if so every particles spawn by this
 * emitter will be render with that texture.
 * <p>A {@link Consumer} of {@link Particle} must be passed in when creating
 * the emitter it will be call for each spawned particle. Its purpose is to
 * initialize the particle.
 * <p>Each emitter holds its own particle pool to avoid instantiating to much
 * objects.
 */
public class ParticleEmitter {

    public static final int MAX_PARTICLE_COUNT = 10000;

    private final Texture texture;
    private final BlendMode blendMode;
    private final float rate;
    private final int batchSize;

    private final Particle[] pool;
    private float timeToNextBatch;
    private final Consumer<Particle> initializer;

    public ParticleEmitter(Texture texture, BlendMode blendMode, float rate, int batchSize, Consumer<Particle> initializer) {
        this.texture = texture;
        this.blendMode = blendMode;
        this.rate = rate;
        this.batchSize = batchSize;
        this.timeToNextBatch = rate;
        this.initializer = initializer;
        this.pool = initPool();
    }

    public ParticleEmitter(BlendMode blendMode, float rate, int batchSize, Consumer<Particle> initializer) {
        this(null, blendMode, rate, batchSize, initializer);
    }

    private static Particle[] initPool() {
        final Particle[] pool = new Particle[MAX_PARTICLE_COUNT];
        for(int i = 0; i < MAX_PARTICLE_COUNT; i++) {
            pool[i] = new Particle();
        }
        return pool;
    }

    /**
     * Updates all the particles owned by this emitter and
     * generates a new batch of particles if enough time as
     * passed.
     * @param time Game time.
     */
    public void update(Time time) {
        for(Particle p : this.pool) {
            p.update(time);
        }

        this.timeToNextBatch -= time.getElapsedTime();
        if(this.timeToNextBatch <= 0) {
            this.generateBatch();
        }
    }

    private void generateBatch() {
        this.timeToNextBatch = this.rate;
        int generated = 0;
        for(int i = 0; i < MAX_PARTICLE_COUNT; i++) {
            if(generated == this.batchSize) {
                break;
            }

            final Particle p = this.pool[i];
            if(!p.isAlive()) {
                this.initializer.accept(p);
                generated++;
            }
        }
    }

    public Optional<Texture> getTexture() {
        return Optional.ofNullable(this.texture);
    }

    public BlendMode getBlendMode() {
        return this.blendMode;
    }

    public Particle[] getParticles() {
        return this.pool;
    }

}
