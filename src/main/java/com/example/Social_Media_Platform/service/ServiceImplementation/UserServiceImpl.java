package com.example.Social_Media_Platform.service.ServiceImplementation;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.example.Social_Media_Platform.dtos.UserDto;
import com.example.Social_Media_Platform.dtos.UserProfileDto;
import com.example.Social_Media_Platform.exception.UserNotFoundException;
import com.example.Social_Media_Platform.model.Friend;
import com.example.Social_Media_Platform.model.Post;
import com.example.Social_Media_Platform.model.User;
import com.example.Social_Media_Platform.repository.FriendRepository;
import com.example.Social_Media_Platform.repository.PostRepository;
import com.example.Social_Media_Platform.repository.UserRepository;
import com.example.Social_Media_Platform.service.AuthService;
import com.example.Social_Media_Platform.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final PostRepository postRepository;
        private final FriendRepository friendRepository;
        private final FileStorageService fileStorageService;
        private final AuthService authService;
        private final ModelMapper modelMapper;

        public UserServiceImpl(UserRepository userRepository, PostRepository postRepository,
                               FriendRepository friendRepository, FileStorageService fileStorageService,
                               AuthService authService, ModelMapper modelMapper) {
            this.userRepository = userRepository;
            this.postRepository = postRepository;
            this.friendRepository = friendRepository;
            this.fileStorageService = fileStorageService;
            this.authService = authService;
            this.modelMapper = modelMapper;
        }

        @Override
        public UserProfileDto getCurrentUserProfile(String token) {
            User user = authService.getCurrentUser(token);
            System.out.println(user);
            return getUserProfile(user.getId());
        }

    @Override
    public UserProfileDto getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Post> postResult = postRepository.findByUserId(userId, 1, null);
        int postCount = postResult.isEmpty() ? 0 : postResult.size();
                postRepository.countPostsByUserId(userId); // You'll need to implement this method

        List<Friend> friendResult = friendRepository.findFriendsByUserId(userId, 1, null);
        int friendCount = friendResult.isEmpty() ? 0 : friendResult.size();
                friendRepository.countFriendsByUserId(userId); // You'll need to implement this method

        UserProfileDto profileDto = modelMapper.map(user, UserProfileDto.class);
        profileDto.setFriendCount(friendCount);
        profileDto.setPostCount(postCount);

        return profileDto;
    }

        @Transactional
        @Override
        public User updateUserProfile(String token, UserDto updateDto) {
            User user = authService.getCurrentUser(token);

            if (updateDto.getBio() != null) {
                user.setBio(updateDto.getBio());
            }

            user.setUpdatedAt(new Date());
            return userRepository.save(user);
        }

        @Transactional
        @Override
        public User updateUserAvatar(String token, MultipartFile file) throws IOException {
            User user = authService.getCurrentUser(token);

            String fileName = fileStorageService.storeFile(file);
            user.setAvatarUrl("/uploads/" + fileName);
            user.setUpdatedAt(new Date());

            return userRepository.save(user);
        }
}
