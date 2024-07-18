package com.springboot.FlomadAIplanner.validator;

import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.exceptions.UserAlreadyExistsException;
import com.springboot.FlomadAIplanner.exceptions.UserNotFoundException;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public void validateUserDoesNotExist(UserDTO userDTO) throws UserAlreadyExistsException {
        if (userService.existsByUsername(userDTO.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + userDTO.getUsername() + " already exists.");
        }
        if (userService.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + userDTO.getEmail() + " already exists.");
        }
    }

    public void validateUserPasswordMatches(User user, String password) throws UserNotFoundException {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException("Invalid username or password.");
        }
    }
}
