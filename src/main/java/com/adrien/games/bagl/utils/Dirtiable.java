package com.adrien.games.bagl.utils;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Defines an element that can be made dirty.
 * <p>
 * A dirty element has to be cleaned up before it is used.
 */
public class Dirtiable<T> {

    private final T dirtiable;
    private final Consumer<T> cleaner;
    private boolean isDirty;

    /**
     * Creates a 'dirtiable' element.
     *
     * The element is considered dirty at initialization. It means
     * that it will be computed when accessed for the first time.
     *
     * @param dirtiable The element.
     * @param cleaner   The cleaner function.
     */
    public Dirtiable(final T dirtiable, final Consumer<T> cleaner) {
        this.dirtiable = Objects.requireNonNull(dirtiable);
        this.cleaner = Objects.requireNonNull(cleaner);
        this.isDirty = true;
    }

    /**
     * Retrieve the cleaned up element.
     * <p>
     * It means that if the element is dirty then its cleaner
     * function will be applied to it before its returned. If
     * the element is not dirty, it will be returned as is.
     *
     * @return The cleaned up element.
     */
    public T get() {
        if (this.isDirty) {
            this.cleaner.accept(dirtiable);
            this.isDirty = false;
        }
        return this.dirtiable;
    }

    /**
     * Makes the element dirty.
     */
    public void dirty() {
        this.isDirty = true;
    }
}
