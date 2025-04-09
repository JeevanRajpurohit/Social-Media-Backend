package com.example.Social_Media_Platform.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.example.Social_Media_Platform.Util.DateConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Friends")
public class Friend {
    @DynamoDBHashKey
    private String id; // Combination of userId and friendId: "userId#friendId"

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "userId-createdAt-index")
    private String userId;

    @DynamoDBAttribute
    private String friendId;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "userId-createdAt-index")
    @DynamoDBTypeConverted(converter = DateConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}