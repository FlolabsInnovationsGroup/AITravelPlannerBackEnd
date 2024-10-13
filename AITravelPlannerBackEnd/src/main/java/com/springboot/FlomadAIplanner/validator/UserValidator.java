package com.springboot.FlomadAIplanner.validator;

import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.exceptions.validators.UserAlreadyExistsException;
import com.springboot.FlomadAIplanner.exceptions.validators.UserNotFoundException;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public void validateUserDoesNotExist(UserDTO userDTO) throws UserAlreadyExistsException {
        if (userService.existsByUsername(userDTO.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + userDTO.getUsername() + " already exists.");
        }
        if (userService.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + userDTO.getEmail() + " already exists.");
        }
    }

    public User validateOAuthUserDoesNotExist(UserDTO userDTO) {
        User user = userService.findByUsernameOrEmail(userDTO.getUsername());
        if (user == null) {
            user = userService.findByUsernameOrEmail(userDTO.getEmail());
        }

        return user;
    }

    public void validateUserPasswordMatches(User user, String password) throws UserNotFoundException {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException("Invalid username or password.");
        }
    }
}
