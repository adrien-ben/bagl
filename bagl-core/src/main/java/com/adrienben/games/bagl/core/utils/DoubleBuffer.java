package com.adrienben.games.bagl.core.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Double buffer
 *
 * @author adrien
 */
public class DoubleBuffer<T> {

    private T readBuffer;

    private T writeBuffer;

    /**
     * Constructs the double buffer by initializing the read and write buffer
     * by calling a supplier function
     *
     * @param supplier The initializer function for the buffers
     */
    public DoubleBuffer(final Supplier<T> supplier) {
        this.readBuffer = supplier.get();
        this.writeBuffer = supplier.get();
    }

    /**
     * Swaps the read and write buffers
     */
    public void swap() {
        final var tmp = this.readBuffer;
        this.readBuffer = this.writeBuffer;
        this.writeBuffer = tmp;
    }

    /**
     * Applies a consumer to both buffers
     * <p>
     * Can be useful to reset the buffer for example
     *
     * @param consumer The consumer to apply on both buffers
     */
    public void apply(final Consumer<T> consumer) {
        consumer.accept(this.readBuffer);
        consumer.accept(this.writeBuffer);
    }

    public T getReadBuffer() {
        return readBuffer;
    }

    public T getWriteBuffer() {
        return writeBuffer;
    }

}
