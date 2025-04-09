package com.example.Social_Media_Platform.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Tokens")
public class Token {

    @DynamoDBHashKey
    private String id;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "userId-index", attributeName = "userId")
    private String userId;

    @DynamoDBAttribute
    private String token;

    @DynamoDBAttribute
    private String tokenType; // ACCESS, REFRESH

    @DynamoDBAttribute
    private boolean revoked;

    @DynamoDBAttribute
    private boolean expired;

    @DynamoDBAttribute
    private Date createdAt;
}
