package com.adrien.games.bagl.core.utils;

/**
 * Tuple2 class
 *
 * @author adrien
 */
public class Tuple2<R, T> {

    private R first;
    private T second;

    /**
     * Construct a tuple
     *
     * @param first  The first element
     * @param second The second element
     */
    public Tuple2(final R first, final T second) {
        this.first = first;
        this.second = second;
    }

    public R getFirst() {
        return this.first;
    }

    public T getSecond() {
        return this.second;
    }
}
