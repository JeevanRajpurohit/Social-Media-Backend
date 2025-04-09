package com.example.Social_Media_Platform.service.ServiceImplementation;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.PaginationResponse;
import com.example.Social_Media_Platform.dtos.PostDto;
import com.example.Social_Media_Platform.exception.PostNotFoundException;
import com.example.Social_Media_Platform.exception.UserNotFoundException;
import com.example.Social_Media_Platform.model.Friend;
import com.example.Social_Media_Platform.model.Post;
import com.example.Social_Media_Platform.model.User;
import com.example.Social_Media_Platform.repository.FriendRepository;
import com.example.Social_Media_Platform.repository.PostRepository;
import com.example.Social_Media_Platform.repository.UserRepository;
import com.example.Social_Media_Platform.service.AuthService;
import com.example.Social_Media_Platform.service.PostService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final FriendRepository friendRepository;
    private final ModelMapper modelMapper;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, AuthService authService, FileStorageService fileStorageService, FriendRepository friendRepository, ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.fileStorageService = fileStorageService;
        this.friendRepository = friendRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public PostDto createPost(String token, String content, MultipartFile image) throws IOException {
        User user = authService.getCurrentUser(token);

        Post post = new Post();
        post.setUserId(user.getId());
        post.setContent(content);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorageService.storeFile(image);
            post.setImageUrl("/uploads/" + fileName);
        }

        Post savedPost = postRepository.save(post);
        return mapToPostDto(savedPost, user);
    }

    @Override
    public PostDto getPostById(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> PostNotFoundException.withId(postId));

        User user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> UserNotFoundException.withId(post.getUserId()));

        return mapToPostDto(post, user);
    }

    @Override
    public PaginationResponse getPostsByUserId(String userId, int limit, String lastEvaluatedKey) {
        Map<String, AttributeValue> exclusiveStartKey = getExclusiveStartKey(lastEvaluatedKey);
        QueryResultPage<Post> scanResult = postRepository.findByUserId(userId, limit, exclusiveStartKey);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.withId(userId));

        List<PostDto> posts = scanResult.getResults().stream()
                .map(post -> mapToPostDto(post, user))
                .collect(Collectors.toList());

        String nextKey = null;
        if (scanResult.getLastEvaluatedKey() != null) {
            if (scanResult.getLastEvaluatedKey().get("createdAt") != null) {
                nextKey = scanResult.getLastEvaluatedKey().get("createdAt").getN();
            } else if (scanResult.getLastEvaluatedKey().get("id") != null) {
                nextKey = scanResult.getLastEvaluatedKey().get("id").getS();
            }
        }

        return new PaginationResponse(
                posts,
                nextKey,
                limit,
                scanResult.getLastEvaluatedKey() != null
        );
    }

    private Map<String, AttributeValue> getExclusiveStartKey(String lastEvaluatedKey) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) {
            return null;
        }

        Map<String, AttributeValue> exclusiveStartKey = new HashMap<>();

        try {
            Long timestamp = Long.parseLong(lastEvaluatedKey);
            exclusiveStartKey.put("createdAt", new AttributeValue().withN(timestamp.toString()));
        } catch (NumberFormatException e) {
            exclusiveStartKey.put("id", new AttributeValue().withS(lastEvaluatedKey));
        }

        return exclusiveStartKey;
    }

    @Override
    public List<PostDto> getNewsFeed(String token, int limit) {
        User user = authService.getCurrentUser(token);

        List<String> friendIds = getFriendIds(user.getId());
        if (friendIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Post> posts = postRepository.findPostsByUserIds(friendIds, limit);

        Map<String, User> users = new HashMap<>();
        for (Post post : posts) {
            users.computeIfAbsent(post.getUserId(), id ->
                    userRepository.findById(id)
                            .orElseThrow(() -> UserNotFoundException.withId(id))
            );
        }

        return posts.stream()
                .map(post -> mapToPostDto(post, users.get(post.getUserId())))
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void deletePost(String token, String postId) {
        User user = authService.getCurrentUser(token);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> PostNotFoundException.withId(postId));

        if (!post.getUserId().equals(user.getId())) {
            throw new PostNotFoundException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    @Transactional
    @Override
    public PostDto likePost(String token, String postId) {
        User user = authService.getCurrentUser(token);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> PostNotFoundException.withId(postId));

        postRepository.incrementLikeCount(postId);
        User postUser = userRepository.findById(post.getUserId())
                .orElseThrow(() -> UserNotFoundException.withId(post.getUserId()));

        return mapToPostDto(post, postUser);
    }

    @Transactional
    @Override
    public PostDto unlikePost(String token, String postId) {
        User user = authService.getCurrentUser(token);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> PostNotFoundException.withId(postId));

        postRepository.decrementLikeCount(postId);
        User postUser = userRepository.findById(post.getUserId())
                .orElseThrow(() -> UserNotFoundException.withId(post.getUserId()));

        return mapToPostDto(post, postUser);
    }

    private List<String> getFriendIds(String userId) {
        QueryResultPage<Friend> result = friendRepository.findFriendsByUserId(userId, 100, null);
        return result.getResults().stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());
    }

    @Override
    public PostDto mapToPostDto(Post post, User user) {
        PostDto postDto = modelMapper.map(post, PostDto.class);
        modelMapper.map(user, postDto);
        return postDto;
    }
}