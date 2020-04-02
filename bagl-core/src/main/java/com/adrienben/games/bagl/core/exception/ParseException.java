package com.adrienben.games.bagl.core.exception;

/**
 * Exception to throw when parsing fails
 *
 * @author adrien
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a parse exception
     *
     * @param message The error message
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Construct a parse exception
     *
     * @param message The error message
     * @param cause   The cause of the exception
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
