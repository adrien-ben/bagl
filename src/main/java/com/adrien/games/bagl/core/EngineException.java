package com.adrien.games.bagl.core;

/**
 * Exception thrown when a error preventing the application to run properly occurs.
 */
public class EngineException extends RuntimeException {

    public EngineException(String message) {
        super(message);
    }

    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
