package com.example.Social_Media_Platform.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.model.Friend;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FriendRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public FriendRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Friend save(Friend friend) {
        friend.setId(generateFriendId(friend.getUserId(), friend.getFriendId()));
        dynamoDBMapper.save(friend);
        return friend;
    }

    public Optional<Friend> findById(String userId, String friendId) {
        return Optional.ofNullable(dynamoDBMapper.load(Friend.class, generateFriendId(userId, friendId)));
    }

    public Optional<Friend> findById(String id) {
        return Optional.ofNullable(dynamoDBMapper.load(Friend.class, id));
    }

    public void delete(Friend friend) {
        dynamoDBMapper.delete(friend);
    }

    public QueryResultPage<Friend> findFriendsByUserId(String userId, int limit, Map<String, AttributeValue> lastEvaluatedKey) {
        Friend friend = new Friend();
        friend.setUserId(userId);

        DynamoDBQueryExpression<Friend> queryExpression = new DynamoDBQueryExpression<Friend>()
                .withHashKeyValues(friend)
                .withIndexName("userId-createdAt-index")
                .withConsistentRead(false)
                .withLimit(limit)
                .withExclusiveStartKey(lastEvaluatedKey)
                .withScanIndexForward(false);

        return dynamoDBMapper.queryPage(Friend.class, queryExpression);
    }

    public List<Friend> findAllFriendsByUserId(String userId) {
        List<Friend> allFriends = new ArrayList<>();
        Map<String, AttributeValue> lastEvaluatedKey = null;

        do {
            QueryResultPage<Friend> result = findFriendsByUserId(userId, 100, lastEvaluatedKey);
            allFriends.addAll(result.getResults());
            lastEvaluatedKey = result.getLastEvaluatedKey();
        } while (lastEvaluatedKey != null);

        return allFriends;
    }
    public int countFriendsByUserId(String userId) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("userId = :userId")
                .withExpressionAttributeValues(
                        Collections.singletonMap(":userId", new AttributeValue().withS(userId))
                );

        return dynamoDBMapper.count(Friend.class, scanExpression);
    }

    private String generateFriendId(String userId, String friendId) {
        return userId + "#" + friendId;
    }
}