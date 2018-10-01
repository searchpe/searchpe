package io.searchpe.api.core.exceptions;

public class StorageException extends Exception {

    /**
     * Constructor.
     */
    public StorageException() {
    }

    /**
     * Constructor.
     * @param message the exception message
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param cause the exception cause the exception cause
     */
    public StorageException(Throwable cause) {
        super(cause);
    }

}
