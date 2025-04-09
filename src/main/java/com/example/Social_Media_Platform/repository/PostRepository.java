package com.example.Social_Media_Platform.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.model.Post;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PostRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public PostRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Post save(Post post) {
        dynamoDBMapper.save(post);
        return post;
    }

    public Optional<Post> findById(String id) {
        return Optional.ofNullable(dynamoDBMapper.load(Post.class, id));
    }

    public void delete(Post post) {
        dynamoDBMapper.delete(post);
    }

    public int countPostsByUserId(String userId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":userId", new AttributeValue().withS(userId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("userId = :userId")
                .withExpressionAttributeValues(eav);

        return dynamoDBMapper.count(Post.class, scanExpression);
    }

    public QueryResultPage<Post> findByUserId(String userId, int limit, Map<String, AttributeValue> lastEvaluatedKey) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":userId", new AttributeValue().withS(userId));

        DynamoDBQueryExpression<Post> queryExpression = new DynamoDBQueryExpression<Post>()
                .withIndexName("userId-createdAt-index")
                .withConsistentRead(false)
                .withKeyConditionExpression("userId = :userId")
                .withExpressionAttributeValues(eav)
                .withLimit(limit)
                .withExclusiveStartKey(lastEvaluatedKey)
                .withScanIndexForward(false);

        return dynamoDBMapper.queryPage(Post.class, queryExpression);
    }



    public List<Post> findPostsByUserIds(List<String> userIds, int limit) {
        List<Post> posts = new ArrayList<>();

        for (String userId : userIds) {
            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":uid", new AttributeValue().withS(userId));

            DynamoDBQueryExpression<Post> queryExpression = new DynamoDBQueryExpression<Post>()
                    .withIndexName("userId-createdAt-index")
                    .withConsistentRead(false)
                    .withKeyConditionExpression("userId = :uid")
                    .withExpressionAttributeValues(eav)
                    .withLimit(limit)
                    .withScanIndexForward(false);

            List<Post> userPosts = dynamoDBMapper.query(Post.class, queryExpression);
            posts.addAll(userPosts);
        }

        posts.sort(Comparator.comparing(Post::getCreatedAt).reversed());
        return posts.stream().limit(limit).collect(Collectors.toList());
    }


    public void incrementLikeCount(String postId) {
        Post post = dynamoDBMapper.load(Post.class, postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            dynamoDBMapper.save(post);
        }
    }

    public void decrementLikeCount(String postId) {
        Post post = dynamoDBMapper.load(Post.class, postId);
        if (post != null) {
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            dynamoDBMapper.save(post);
        }
    }

    public void incrementCommentCount(String postId) {
        Post post = dynamoDBMapper.load(Post.class, postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            dynamoDBMapper.save(post);
        }
    }

    public void decrementCommentCount(String postId) {
        Post post = dynamoDBMapper.load(Post.class, postId);
        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            dynamoDBMapper.save(post);
        }
    }
}