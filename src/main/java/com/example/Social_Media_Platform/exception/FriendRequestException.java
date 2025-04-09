package com.example.Social_Media_Platform.exception;

import com.example.Social_Media_Platform.service.FriendService;

public class FriendRequestException extends RuntimeException{
    public FriendRequestException(String msg){
        super(msg);
    }
}
