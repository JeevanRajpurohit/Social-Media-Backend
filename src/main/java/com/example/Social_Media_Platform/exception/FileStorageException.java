package com.example.Social_Media_Platform.exception;

import java.io.IOException;

public class FileStorageException extends RuntimeException{
    public FileStorageException(String msg, IOException ex){
        super(msg);
    }
}
