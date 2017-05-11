package com.adrien.games.bagl.rendering.particles;

import com.adrien.games.bagl.core.Time;

import java.util.function.Consumer;

public class ParticleEmitter {

    public static final int MAX_PARTICLE_COUNT = 10000;

    private final float rate;
    private final int batchSize;

    private final Particle[] pool;
    private float timeToNextBatch;
    private final Consumer<Particle> initializer;

    public ParticleEmitter(float rate, int batchSize, Consumer<Particle> initializer) {
        this.rate = rate;
        this.batchSize = batchSize;
        this.pool = initPool();
        this.timeToNextBatch = rate;
        this.initializer = initializer;
    }

    private static Particle[] initPool() {
        final Particle[] pool = new Particle[MAX_PARTICLE_COUNT];
        for(int i = 0; i < MAX_PARTICLE_COUNT; i++) {
            pool[i] = new Particle();
        }
        return pool;
    }

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

    public Particle[] getParticles() {
        return this.pool;
    }

}
