package com.example.Social_Media_Platform.exception;

public class DuplicateUsernameException extends RuntimeException{
    public DuplicateUsernameException(String msg){
        super(msg);
    }
}