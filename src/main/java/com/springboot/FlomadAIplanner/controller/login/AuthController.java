package com.springboot.FlomadAIplanner.controller.login;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.dto.auth.LoginRequest;
import com.springboot.FlomadAIplanner.dto.auth.LoginResponse;
import com.springboot.FlomadAIplanner.exceptions.UserAlreadyExistsException;
import com.springboot.FlomadAIplanner.exceptions.UserNotFoundException;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.login.UserService;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final Converter<User, UserDetails> userConverter;
    private final Converter<UserDTO, User> userDTOToUserConverter;
    private final JwtUtil jwtUtil;
    private final UserValidator userValidator;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody UserDTO userDTO) {
        try {
            userValidator.validateUserDoesNotExist(userDTO);

            User user = userDTOToUserConverter.convert(userDTO);
            userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully. Please check your email for verification instructions.");
        } catch (UserAlreadyExistsException e) {
            log.error("Login failed for user: {}. Error: {}", userDTO.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Sign-up failed: " + e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        User user = userService.findByVerificationToken(token);
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }
        userService.verifyUser(user);
        return ResponseEntity.ok("Email verified successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Attempting login for user: {}", loginRequest.getUsername());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            userValidator.validateUserPasswordMatches(userService.findUserByUsername(loginRequest.getUsername()), loginRequest.getPassword());

            final User user = userService.findUserByUsername(loginRequest.getUsername());
            final UserDetails userDetails = userConverter.convert(user);
            final String jwtToken = jwtUtil.generateToken(userDetails);

            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new LoginResponse(user.isVerified(), jwtToken, "Login successful"));
        } catch (AuthenticationException | UserNotFoundException e) {
            log.error("Login failed for user: {}. Error: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, "Login failed: " + e.getMessage()));
        }
    }
}
