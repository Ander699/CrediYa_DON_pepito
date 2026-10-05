package com.crediya.exception;

public class CrediYaException extends RuntimeException {
    public CrediYaException(String mensaje) { super(mensaje); }
    public CrediYaException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
