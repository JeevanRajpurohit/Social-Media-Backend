package com.example.Social_Media_Platform.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.model.Token;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TokenRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public TokenRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Token save(Token token) {
        dynamoDBMapper.save(token);
        return token;
    }

    public Optional<Token> findByToken(String token) {
        Token tokenObj = new Token();
        tokenObj.setToken(token);

        DynamoDBQueryExpression<Token> queryExpression = new DynamoDBQueryExpression<Token>()
                .withHashKeyValues(tokenObj);

        List<Token> results = dynamoDBMapper.query(Token.class, queryExpression);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Token> findAllValidTokensByUser(String userId) {
        DynamoDBQueryExpression<Token> queryExpression = new DynamoDBQueryExpression<Token>()
                .withIndexName("userId-index")
                .withConsistentRead(false)
                .withKeyConditionExpression("userId = :uid")
                .withFilterExpression("expired = :expired AND revoked = :revoked")
                .withExpressionAttributeValues(Map.of(
                        ":uid", new AttributeValue().withS(userId),
                        ":expired", new AttributeValue().withBOOL(false),
                        ":revoked", new AttributeValue().withBOOL(false)
                ));

        return dynamoDBMapper.query(Token.class, queryExpression);
    }



    public void revokeAllUserTokens(String userId) {
        List<Token> validTokens = findAllValidTokensByUser(userId);
        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
            dynamoDBMapper.save(token);
        });
    }
}