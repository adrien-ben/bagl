package com.adrien.games.bagl.core.math;

import org.lwjgl.stb.STBPerlin;

/**
 * Noise generator.
 */
public class Noise {

    /**
     * Compute 3D perlin noise.
     * <p>
     * Result is in [0, 1]
     *
     * @param x           The x coordinate.
     * @param y           The y coordinate.
     * @param z           The z coordinate.
     * @param octaves     The number of octave.
     * @param persistence The noise persistence.
     * @return The computed noise value.
     */
    public static float perlin(final float x, final float y, final float z, final int octaves, final float persistence) {
        var total = 0;
        var frequency = 1;
        var amplitude = 1;
        var maxValue = 0;
        for (var i = 0; i < octaves; i++) {
            final var noise = perlin(x * frequency, y * frequency, z * frequency);
            total += noise * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        return total / maxValue;
    }

    /**
     * Compute 3D perlin noise.
     * <p>
     * Result is in [0, 1]
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return The computed noise value.
     */
    public static float perlin(final float x, final float y, final float z) {
        return STBPerlin.stb_perlin_noise3(x, y, z, 0, 0, 0) * 0.5f + 0.5f;
    }
}
