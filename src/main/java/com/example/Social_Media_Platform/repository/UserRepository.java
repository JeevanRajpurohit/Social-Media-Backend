package com.example.Social_Media_Platform.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.model.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public UserRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public User save(User user) {
        dynamoDBMapper.save(user);
        return user;
    }

    public Optional<User> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        try {
            return Optional.ofNullable(dynamoDBMapper.load(User.class, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public Optional<User> findByEmail(String email) {
        User user = new User();
        user.setEmail(email);

        DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>()
                .withIndexName("email-index")
                .withConsistentRead(false)
                .withHashKeyValues(user);

        List<User> results = dynamoDBMapper.query(User.class, queryExpression);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> findByUsername(String username) {
        User user = new User();
        user.setUsername(username);

        DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>()
                .withIndexName("username-index")
                .withConsistentRead(false)
                .withHashKeyValues(user);

        List<User> results = dynamoDBMapper.query(User.class, queryExpression);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}