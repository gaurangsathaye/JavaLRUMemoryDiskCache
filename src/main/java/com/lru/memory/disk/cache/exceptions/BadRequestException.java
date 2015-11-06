package com.lru.memory.disk.cache.exceptions;

/**
 *
 * @author sathayeg
 */
public class BadRequestException extends Exception {
    public BadRequestException(String message, Throwable cause){
        super(message, cause);
    }
}
