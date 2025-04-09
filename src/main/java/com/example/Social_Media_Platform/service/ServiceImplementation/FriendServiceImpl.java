package com.example.Social_Media_Platform.service.ServiceImplementation;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.Social_Media_Platform.Util.PaginationResponse;
import com.example.Social_Media_Platform.dtos.FriendRequestDto;
import com.example.Social_Media_Platform.dtos.FriendResponseDto;
import com.example.Social_Media_Platform.exception.FriendRequestException;
import com.example.Social_Media_Platform.exception.UserNotFoundException;
import com.example.Social_Media_Platform.model.Comment;
import com.example.Social_Media_Platform.model.Friend;
import com.example.Social_Media_Platform.model.FriendRequest;
import com.example.Social_Media_Platform.model.User;
import com.example.Social_Media_Platform.repository.FriendRepository;
import com.example.Social_Media_Platform.repository.FriendRequestRepository;
import com.example.Social_Media_Platform.repository.UserRepository;
import com.example.Social_Media_Platform.service.AuthService;
import com.example.Social_Media_Platform.service.FriendService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ModelMapper modelMapper;

    public FriendServiceImpl(FriendRepository friendRepository, FriendRequestRepository friendRequestRepository,
                             UserRepository userRepository, AuthService authService, ModelMapper modelMapper) {
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    @Override
    public FriendRequest sendFriendRequest(String token, String receiverId) {
        User sender = authService.getCurrentUser(token);

        if (sender.getId().equals(receiverId)) {
            throw new FriendRequestException("Cannot send friend request to yourself");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> UserNotFoundException.withId(receiverId));

        if (friendRepository.findById(sender.getId(), receiverId).isPresent()) {
            throw new FriendRequestException("Already friends");
        }

        List<FriendRequest> existingRequests = friendRequestRepository.findBySenderIdOrReceiverId(sender.getId(), 10, null);
        for (FriendRequest req : existingRequests) {
            if ((req.getSenderId().equals(sender.getId()) && req.getReceiverId().equals(receiverId)) ||
                    (req.getSenderId().equals(receiverId) && req.getReceiverId().equals(sender.getId()))) {
                if (req.getStatus().equals("Pending")) {
                    throw new FriendRequestException("Friend request already pending");
                }
                if (req.getStatus().equals("Accepted")) {
                    throw new FriendRequestException("Already friends");
                }
            }
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(sender.getId());
        friendRequest.setReceiverId(receiverId);
        friendRequest.setStatus("Pending");
        friendRequest.setCreatedAt(new Date());

        return friendRequestRepository.save(friendRequest);
    }

    @Override
    public FriendRequest respondToFriendRequest(String token, String requestId, String status) {
        User user = authService.getCurrentUser(token);
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestException("Friend request not found"));

        if (!friendRequest.getReceiverId().equals(user.getId())) {
            throw new FriendRequestException("You can only respond to requests sent to you");
        }

        if (!friendRequest.getStatus().equals("Pending")) {
            throw new FriendRequestException("Friend request already processed");
        }

        if (!status.equals("Accepted") && !status.equals("Rejected")) {
            throw new FriendRequestException("Invalid status");
        }

        friendRequest.setStatus(status);

        if (status.equals("Accepted")) {
            Friend friendship1 = new Friend();
            friendship1.setId(generateFriendId(friendRequest.getSenderId(), friendRequest.getReceiverId()));
            friendship1.setUserId(friendRequest.getSenderId());
            friendship1.setFriendId(friendRequest.getReceiverId());
            friendship1.setCreatedAt(new Date());
            friendRepository.save(friendship1);

            Friend friendship2 = new Friend();
            friendship2.setId(generateFriendId(friendRequest.getReceiverId(), friendRequest.getSenderId()));
            friendship2.setUserId(friendRequest.getReceiverId());
            friendship2.setFriendId(friendRequest.getSenderId());
            friendship2.setCreatedAt(new Date());
            friendRepository.save(friendship2);
        }
        friendRequestRepository.delete(friendRequest);

        return friendRequest;
    }

    @Override
    public List<FriendRequestDto> getFriendRequests(String token, String status, int limit,
                                                    Map<String, AttributeValue> lastEvaluatedKey) {
        User user = authService.getCurrentUser(token);
        List<FriendRequest> requests;
        if (status != null) {
            requests = friendRequestRepository.findByReceiverIdAndStatus(user.getId(), status, limit, lastEvaluatedKey);
        } else {
            requests = friendRequestRepository.findBySenderIdOrReceiverId(user.getId(), limit, lastEvaluatedKey);
        }

        Set<String> userIds = new HashSet<>();
        for (FriendRequest req : requests) {
            userIds.add(req.getSenderId());
            userIds.add(req.getReceiverId());
        }

        Map<String, User> users = new HashMap<>();
        for (String id : userIds) {
            User userDetail = userRepository.findById(id)
                    .orElseThrow(() -> UserNotFoundException.withId(id));
            users.put(id, userDetail);
        }

        return requests.stream()
                .map(req -> mapToFriendRequestDto(req, users))
                .collect(Collectors.toList());
    }

    @Override
    public PaginationResponse getFriends(String token, int limit, String lastEvaluatedKey) {
        User user = authService.getCurrentUser(token);
        List<Friend>list=friendRepository.findFriendsByUserId(user.getId(),limit,lastEvaluatedKey);
        boolean hasMore=!(list.size()<limit);
        lastEvaluatedKey = hasMore ? list.get(list.size() - 1).getId() : null;
        return new PaginationResponse(list,lastEvaluatedKey,limit,hasMore);
    }


    @Override
    public void removeFriend(String token, String friendId) {
        User user = authService.getCurrentUser(token);

        friendRepository.findById(generateFriendId(user.getId(), friendId)).ifPresent(friendRepository::delete);
        friendRepository.findById(generateFriendId(friendId, user.getId())).ifPresent(friendRepository::delete);

        deleteFriendRequestsBetweenUsers(user.getId(), friendId);
    }

    private void deleteFriendRequestsBetweenUsers(String userId1, String userId2) {
        List<FriendRequest> requests = friendRequestRepository.findRequestsBetweenUsers(userId1, userId2);
        requests.forEach(friendRequestRepository::delete);
    }

    @Override
    public FriendRequestDto mapToFriendRequestDto(FriendRequest request, Map<String, User> users) {
        FriendRequestDto dto = modelMapper.map(request, FriendRequestDto.class);
        modelMapper.map(users.get(request.getSenderId()), dto);
        modelMapper.map(users.get(request.getReceiverId()), dto);
        return dto;
    }

    @Override
    public FriendResponseDto mapToFriendResponseDto(Friend friend, Map<String, User> users) {
        FriendResponseDto dto = modelMapper.map(friend, FriendResponseDto.class);
        modelMapper.map(users.get(friend.getFriendId()), dto);
        return dto;
    }

    private String generateFriendId(String userId, String friendId) {
        return userId + "#" + friendId;
    }
}