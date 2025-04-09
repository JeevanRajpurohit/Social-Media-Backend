package com.example.Social_Media_Platform.service.ServiceImplementation;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.PaginationResponse;
import com.example.Social_Media_Platform.dtos.PostDto;
import com.example.Social_Media_Platform.exception.PostNotFoundException;
import com.example.Social_Media_Platform.exception.UserNotFoundException;
import com.example.Social_Media_Platform.model.Comment;
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
        List<Post>list=postRepository.findByUserId(userId,limit,lastEvaluatedKey);
        boolean hasMore=!(list.size()<limit);
        lastEvaluatedKey = hasMore ? list.get(list.size() - 1).getId() : null;
        return new PaginationResponse(list,lastEvaluatedKey,limit,hasMore);
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
        List<Friend> result = friendRepository.findFriendsByUserId(userId, 100, null);
        return result.stream()
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