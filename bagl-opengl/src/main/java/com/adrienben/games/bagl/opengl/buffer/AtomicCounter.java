package com.adrienben.games.bagl.opengl.buffer;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

/**
 * OpenGL atomic counter buffer.
 *
 * @author adrien
 */
public class AtomicCounter {

    private final Buffer buffer;
    private final IntBuffer view;

    public AtomicCounter() {
        this(0);
    }

    public AtomicCounter(final int initialValue) {
        view = MemoryUtil.memAllocInt(1);
        view.put(0, initialValue);
        buffer = new Buffer(view, BufferUsage.DYNAMIC_READ);
    }

    public void destroy() {
        buffer.destroy();
        MemoryUtil.memFree(view);
    }

    public void bind(final int index) {
        buffer.bind(BufferTarget.ATOMIC_COUNTER, index);
    }

    public void unbind(final int index) {
        buffer.unbind(BufferTarget.ATOMIC_COUNTER, index);
    }

    public int getValue() {
        buffer.getSubData(view, 0);
        return view.get(0);
    }

    public void reset() {
        reset(0);
    }

    public void reset(final int initialValue) {
        view.put(0, initialValue);
        buffer.setSubData(view, 0);
    }

}
