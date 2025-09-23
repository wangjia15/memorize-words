package com.memorizewords.dto.response;

import lombok.Data;

/**
 * Generic API response wrapper.
 */
@Data
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private T data;

    private Object errors;

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrors(errors);
        return response;
    }
}