package com.example.Social_Media_Platform.service.ServiceImplementation;

import com.example.Social_Media_Platform.Util.JwtUtil;
import com.example.Social_Media_Platform.dtos.*;
import com.example.Social_Media_Platform.exception.DuplicateEmailException;
import com.example.Social_Media_Platform.exception.DuplicateUsernameException;
import com.example.Social_Media_Platform.exception.InvalidTokenException;
import com.example.Social_Media_Platform.exception.UserNotFoundException;
import com.example.Social_Media_Platform.model.Token;
import com.example.Social_Media_Platform.model.User;
import com.example.Social_Media_Platform.repository.TokenRepository;
import com.example.Social_Media_Platform.repository.UserRepository;
import com.example.Social_Media_Platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final ModelMapper modelMapper;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
                           PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenRepository tokenRepository, ModelMapper modelMapper) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenRepository = tokenRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public User register(UserDto registerDto) {
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already in use");
        }

        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new DuplicateUsernameException("Username already in use");
        }

        User user = modelMapper.map(registerDto, User.class);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setStatus("Active");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        return userRepository.save(user);
    }

    @Override
    public TokenDto login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        String usernameOrEmail = authentication.getName();
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        System.out.println("User ID: " + user.getId());
        tokenRepository.revokeAllUserTokens(user.getId());

        saveUserToken(user, refreshToken, "REFRESH");
        saveUserToken(user,accessToken,"ACCESS");

        return new TokenDto(accessToken, refreshToken);
    }

    @Override
    public TokenDto refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Token storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        saveUserToken(user, newRefreshToken, "REFRESH");

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    @Override
    public User getCurrentUser(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public void saveUserToken(User user, String jwtToken, String tokenType) {
        Token token = new Token();
        token.setId(jwtToken);
        token.setUserId(user.getId());
        token.setToken(jwtToken);
        token.setTokenType(tokenType);
        token.setExpired(false);
        token.setRevoked(false);
        token.setCreatedAt(new Date());
        tokenRepository.save(token);
    }
}
