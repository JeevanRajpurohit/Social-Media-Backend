package com.example.Social_Media_Platform.exception;

public class PostNotFoundException extends RuntimeException{
    public PostNotFoundException(String msg){
        super(msg);
    }
    public static PostNotFoundException withId(String postId) {
        return new PostNotFoundException("Post not found with id: " + postId);
    }
}
