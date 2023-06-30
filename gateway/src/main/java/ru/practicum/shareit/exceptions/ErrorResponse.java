package ru.practicum.shareit.exceptions;

public class ErrorResponse {
    private final String error;
    private String stackTrace;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public ErrorResponse(String error, String stackTrace) {
        this.error = error;
        this.stackTrace = stackTrace;
    }

    public String getError() {
        return error;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}