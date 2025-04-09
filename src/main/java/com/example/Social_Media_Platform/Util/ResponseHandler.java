package com.example.Social_Media_Platform.Util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseHandler {
    private Object data;
    private String message;
    private int status;
    private boolean success;
    private String entity;
}
