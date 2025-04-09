package com.example.Social_Media_Platform.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.example.Social_Media_Platform.model.FriendRequest;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FriendRequestRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public FriendRequestRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public FriendRequest save(FriendRequest friendRequest) {
        friendRequest.setReceiverIdStatus(friendRequest.getReceiverId() + "#" + friendRequest.getStatus());
        dynamoDBMapper.save(friendRequest);
        return friendRequest;
    }

    public Optional<FriendRequest> findById(String id) {
        return Optional.ofNullable(dynamoDBMapper.load(FriendRequest.class, id));
    }

    public void delete(FriendRequest friendRequest) {
        dynamoDBMapper.delete(friendRequest);
    }

    public List<FriendRequest> findByReceiverIdAndStatus(String receiverId, String status, int limit,
                                                         Map<String, AttributeValue> lastEvaluatedKey) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setReceiverIdStatus(receiverId + "#" + status);

        DynamoDBQueryExpression<FriendRequest> queryExpression = new DynamoDBQueryExpression<FriendRequest>()
                .withIndexName("receiver-status-index")
                .withConsistentRead(false)
                .withHashKeyValues(friendRequest)
                .withLimit(limit)
                .withExclusiveStartKey(lastEvaluatedKey)
                .withScanIndexForward(false);

        return dynamoDBMapper.queryPage(FriendRequest.class, queryExpression).getResults();
    }

    public List<FriendRequest> findBySenderIdOrReceiverId(String userId, int limit,
                                                          Map<String, AttributeValue> lastEvaluatedKey) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":userId", new AttributeValue().withS(userId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("senderId = :userId OR receiverId = :userId")
                .withExpressionAttributeValues(eav)
                .withLimit(limit)
                .withExclusiveStartKey(lastEvaluatedKey);

        return dynamoDBMapper.scanPage(FriendRequest.class, scanExpression).getResults();
    }

    public List<FriendRequest> findRequestsBetweenUsers(String userId1, String userId2) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":userId1", new AttributeValue().withS(userId1));
        eav.put(":userId2", new AttributeValue().withS(userId2));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("(senderId = :userId1 AND receiverId = :userId2) OR " +
                        "(senderId = :userId2 AND receiverId = :userId1)")
                .withExpressionAttributeValues(eav);

        return dynamoDBMapper.scanPage(FriendRequest.class, scanExpression).getResults();
    }
}