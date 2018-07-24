package com.adrien.games.bagl.utils.repository;

import java.util.Optional;

/**
 * Repository interface.
 *
 * @author adrien
 */
public interface Repository<ID, T> {

    /**
     * Put an entry in the repository.
     * <p>
     * Previous entry with the same {@code id} will be replaced.
     */
    void put(ID id, T entry);

    /**
     * Remove the entry with id {@code id}.
     * <p>
     * If there is no entry with for {@code id} nothing will append.
     */
    void remove(ID id);

    /**
     * Get the entry with id {@code id}.
     */
    Optional<T> getById(ID id);

}
