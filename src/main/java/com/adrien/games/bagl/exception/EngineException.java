package com.adrien.games.bagl.exception;

/**
 * Exception thrown when a error preventing the application to run properly occurs
 *
 * @author adrien
 */
public class EngineException extends RuntimeException {

    /**
     * Construct an engine exception
     *
     * @param message The error message
     */
    public EngineException(String message) {
        super(message);
    }

    /**
     * Construct an engine exception
     *
     * @param message The error message
     * @param cause   The cause of the exception
     */
    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
