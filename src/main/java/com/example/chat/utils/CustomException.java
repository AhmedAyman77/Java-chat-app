package com.example.chat.utils;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final int statusCode;

    public CustomException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public static CustomException badRequest(String message) {
        return new CustomException(400, message);
    }

    public static CustomException badCredentials() {
        return new CustomException(401, "Bad credentials");
    }

    public static CustomException resourceNotFound(String message) {
        return new CustomException(404, message);
    }

    public static CustomException conflict(String message) {
        return new CustomException(409, message);
    }

    public static CustomException internalServerError(String message) {
        return new CustomException(500, message);
    }
}