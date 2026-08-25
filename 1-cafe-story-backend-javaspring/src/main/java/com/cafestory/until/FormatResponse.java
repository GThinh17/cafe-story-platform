package com.cafestory.until;

import lombok.Data;

@Data
public class FormatResponse<T> {
    private int statusCode;
    private String status;
    private String message;
    private T data;

    public FormatResponse() {
    }

    public FormatResponse(int statusCode, String status, String message, T data) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
