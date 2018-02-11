package ru.ifmo.rain.bandarchuk.walk.exceptions;

public class RecursiveWalkException extends Exception {

    public RecursiveWalkException(String message) {
        super(message);
    }

    public RecursiveWalkException(String message, Throwable cause) {
        super(message, cause);
    }
}
