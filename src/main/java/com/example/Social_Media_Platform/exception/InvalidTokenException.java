package com.example.Social_Media_Platform.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String msg){
        super(msg);
    }
}
