package org.leochel.exceptions;

public class TarantoolOperationException extends RuntimeException{
    public TarantoolOperationException(String operation, Throwable cause) {
        super("Tarantool operation failed: " + operation, cause);
    }
}
