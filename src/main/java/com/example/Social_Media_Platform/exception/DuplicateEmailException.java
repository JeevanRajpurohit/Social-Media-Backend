package com.example.Social_Media_Platform.exception;

public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException(String msg){
        super(msg);
    }
}
