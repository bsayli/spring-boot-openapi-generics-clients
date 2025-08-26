package com.example.demo.common.api.response;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

public record ApiResponse<T>(
        int status,
        String message,
        T data,
        List<ApiError> errors
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), "OK", data, Collections.emptyList());
    }

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data, Collections.emptyList());
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message, null, Collections.emptyList());
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message, List<ApiError> errors) {
        return new ApiResponse<>(status.value(), message, null, errors != null ? errors : Collections.emptyList());
    }
}