package com.example.Social_Media_Platform.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.model.Comment;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Attr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CommentRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public CommentRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Comment save(Comment comment) {
        dynamoDBMapper.save(comment);
        return comment;
    }

    public Optional<Comment> findById(String id) {
        return Optional.ofNullable(dynamoDBMapper.load(Comment.class, id));
    }

    public void delete(Comment comment) {
        dynamoDBMapper.delete(comment);
    }

    public List<Comment> findByPostId(String postId, int limit, String lastEvaluatedKey) {

        Map<String , AttributeValue> exclusiveStartKey=new HashMap<>();

        Map<String,AttributeValue> eav=new HashMap<>();
        eav.put(":postId",new AttributeValue().withS(postId));

        DynamoDBQueryExpression<Comment> queryExpression = new DynamoDBQueryExpression<Comment>()
                .withIndexName("post-comment-index")
                .withKeyConditionExpression("postId = :postId")
                .withExpressionAttributeValues(eav)
                .withConsistentRead(false)
                .withLimit(limit)
                .withScanIndexForward(false);

        if(lastEvaluatedKey!=null)
        {
            exclusiveStartKey.put("postId",new AttributeValue().withS(postId));
            exclusiveStartKey.put("id",new AttributeValue().withS(lastEvaluatedKey));
            queryExpression.setExclusiveStartKey(exclusiveStartKey);
        }

        return dynamoDBMapper.queryPage(Comment.class, queryExpression).getResults();
    }


}