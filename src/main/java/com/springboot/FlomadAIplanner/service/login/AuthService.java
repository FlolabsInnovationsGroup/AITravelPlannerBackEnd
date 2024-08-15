package com.springboot.FlomadAIplanner.service.login;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.exceptions.validators.UserNotFoundException;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.dto.auth.LoginRequest;
import com.springboot.FlomadAIplanner.dto.auth.LoginResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final Converter<User, UserDetails> userConverter;
    private final Converter<UserDTO, User> userDTOToUserConverter;
    private final JwtUtil jwtUtil;
    private final UserValidator userValidator;

    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        try {
            log.info("Attempting login for user: {}", loginRequest.getUsername());

            // Authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            log.info("Authentication successful for user: {}", loginRequest.getUsername());

            // Validate user password matches
            User user = userService.findByUsernameOrEmail(loginRequest.getUsername());
            if (user == null) {
                log.error("User not found: {}", loginRequest.getUsername());
                throw new UserNotFoundException("User not found: " + loginRequest.getUsername());
            }

            log.debug("User found: {}", user);

            userValidator.validateUserPasswordMatches(user, loginRequest.getPassword());
            log.info("Password validation successful for user: {}", loginRequest.getUsername());

            // Convert user to UserDetails
            UserDetails userDetails = userConverter.convert(user);
            String jwtToken = jwtUtil.generateToken(userDetails);

            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new LoginResponse(user.getIsEmailVerified(), jwtToken, "Login successful"));
        } catch (AuthenticationException | UserNotFoundException e) {
            log.error("Login failed for user: {}. Error: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, "Login failed: " + e.getMessage()));
        }
    }

    public ResponseEntity<String> verifyUser(String token) {
        User user = userService.findByVerificationToken(token);
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }
        userService.verifyUser(user);
        return ResponseEntity.ok("Email verified successfully.");
    }

    public User registerUser(UserDTO userDTO) {
        User user = userDTOToUserConverter.convert(userDTO);
        userService.registerUser(user);
        return user;
    }
}
