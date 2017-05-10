package com.adrien.games.bagl.core;

/**
 * Simple time class allowing retrieval of elapsed time since 
 * last frame and total program time.
 *
 */
public final class Time {

    private static final long NS_PER_SEC = 1000000000L;

    private long elapsed;
    private long total;
    private long time;

    public Time() {
        this.elapsed = 0;
        this.total = 0;
        this.time = time();
    }

    /**
     * Updates the timings
     */
    public void update() {
        long newTime = time();
        elapsed = newTime - time;
        total += elapsed;
        time = newTime;
    }

    private long time() {
        return System.nanoTime();
    }

    /**
     * Returns the time of the last frame in seconds.
     * @return The time of the last frame.
     */
    public float getElapsedTime() {
        return (float)elapsed/NS_PER_SEC;
    }

    /**
     * Returns the total time of the program in seconds.
     * @return The total time of the program.
     */
    public float getTotalTime() {
        return (float)total/NS_PER_SEC;
    }

}