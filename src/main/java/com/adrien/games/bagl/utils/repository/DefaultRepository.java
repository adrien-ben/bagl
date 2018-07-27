package com.adrien.games.bagl.utils.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Default repository implementation where data are stored in a map.
 *
 * @author adrien
 */
public class DefaultRepository<ID, T> implements Repository<ID, T> {

    private Map<ID, T> data;

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
    public void put(final ID id, final T entry) {
        data.put(id, entry);
    }

    /**
     * {@inheritDoc}
     *
     * @see Repository#remove(Object)
     */
    @Override
    public void remove(final ID id) {
        data.remove(id);
    }

    /**
     * {@inheritDoc}
     *
     * @see Repository#getById(Object)
     */
    @Override
    public Optional<T> getById(final ID id) {
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
