package com.springboot.FlomadAIplanner.validator;

import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.exceptions.validators.UserAlreadyExistsException;
import com.springboot.FlomadAIplanner.exceptions.validators.UserNotFoundException;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public void validateUserDoesNotExist(UserAuthDTO userAuthDTO) throws UserAlreadyExistsException {
        if (userService.existsByUsername(userAuthDTO.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + userAuthDTO.getUsername() + " already exists.");
        }
        if (userService.existsByEmail(userAuthDTO.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + userAuthDTO.getEmail() + " already exists.");
        }
    }

    public User validateOAuthUserDoesNotExist(UserAuthDTO userAuthDTO) {
        User user = userService.findByUsernameOrEmail(userAuthDTO.getUsername());
        if (user == null) {
            user = userService.findByUsernameOrEmail(userAuthDTO.getEmail());
        }

        return user;
    }

    public void validateUserPasswordMatches(User user, String password) throws UserNotFoundException {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException("Invalid username or password.");
        }
    }
}
