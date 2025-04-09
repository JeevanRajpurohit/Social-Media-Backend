package com.example.Social_Media_Platform.service;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.PaginationResponse;
import com.example.Social_Media_Platform.dtos.FriendRequestDto;
import com.example.Social_Media_Platform.dtos.FriendResponseDto;
import com.example.Social_Media_Platform.dtos.FriendshipDto;
import com.example.Social_Media_Platform.model.Friend;
import com.example.Social_Media_Platform.model.FriendRequest;
import com.example.Social_Media_Platform.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface FriendService {

    @Transactional
    FriendRequest sendFriendRequest(String token, String receiverId);

    @Transactional
    FriendRequest respondToFriendRequest(String token, String requestId, String status);

    List<FriendRequestDto> getFriendRequests(String token, String status, int limit, Map<String, AttributeValue> lastEvaluatedKey);


    PaginationResponse getFriends(String token, int limit, String lastEvaluatedKey);

    @Transactional
    void removeFriend(String token, String friendId);

    FriendRequestDto mapToFriendRequestDto(FriendRequest request, Map<String, User> users);

    FriendResponseDto mapToFriendResponseDto(Friend friend, Map<String, User> users);
}
