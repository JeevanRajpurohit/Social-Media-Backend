package com.example.Social_Media_Platform.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String msg){
        super(msg);
    }
    public static UserNotFoundException withId(String postId) {
        return new UserNotFoundException("Post not found with id: " + postId);
    }
}