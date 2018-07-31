package com.adrienben.games.bagl.core.utils.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Default repository implementation where data are stored in a map.
 *
 * @author adrien
 */
public class DefaultRepository<I, T> implements Repository<I, T> {

    private Map<I, T> data;

    /**
     * Construct a new {@link DefaultRepository} where data are stored in an {@link HashMap}.
     */
    public DefaultRepository() {
        this.data = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * @see Repository#put(Object, Object)
     */
    @Override
    public void put(final I id, final T entry) {
        data.put(id, entry);
    }

    /**
     * {@inheritDoc}
     *
     * @see Repository#remove(Object)
     */
    @Override
    public void remove(final I id) {
        data.remove(id);
    }

    /**
     * {@inheritDoc}
     *
     * @see Repository#getById(Object)
     */
    @Override
    public Optional<T> getById(final I id) {
        return Optional.ofNullable(data.get(id));
    }

    /**
     * {@inheritDoc}
     *
     * @see Repository#getAll()
     */
    @Override
    public Stream<T> getAll() {
        return data.values().stream();
    }

    /**
     * {@inheritDoc}
     *
     * @see Repository#clear()
     */
    @Override
    public void clear() {
        data.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @see Repository#count()
     */
    @Override
    public int count() {
        return data.size();
    }
}
