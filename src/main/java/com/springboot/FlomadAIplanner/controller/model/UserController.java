package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.util.CurrentUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserUtil currentUserUtil;
    private final Converter<User, UserDTO> userConverter;
    private final Converter<UserDTO, User> userRevertConverter;

    public UserController(UserService userService, CurrentUserUtil currentUserUtil,
                          @Qualifier("userToUserDTOConverter") Converter<User, UserDTO> userConverter,
                          @Qualifier("userDTOToUserConverter") Converter<UserDTO, User> userRevertConverter) {
        this.userService = userService;
        this.currentUserUtil = currentUserUtil;
        this.userConverter = userConverter;
        this.userRevertConverter = userRevertConverter;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDTO user) {
        log.info("Creating new user: {}", user);
        User savedUser = userService.save(userRevertConverter.convert(user));
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        log.info("Retrieving user by id: {}", id);
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<Iterable<UserDTO>> getAllUsers() {
        log.info("Retrieving all users");
        Iterable<User> users = userService.findAll();
        // Convert User entities to UserDTOs
        Iterable<UserDTO> userDTOs = StreamSupport.stream(users.spliterator(), false)
                .map(userConverter::convert)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id, @RequestBody UserDTO updatedUser) {
        log.info("Updating user with id: {}", id);
        User user = userService.update(id, userRevertConverter.convert(updatedUser));
        return ResponseEntity.ok(userConverter.convert(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("Deleting user with id: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        log.info("Getting current user from security context");
        User user = currentUserUtil.getCurrentUserFromSecurityContext();
        if (user != null) {
            log.info("Retrieving current user: {}", user.getUsername());
            return ResponseEntity.ok(userConverter.convert(user));
        }
        log.warn("Current user is null, returning 404");
        return ResponseEntity.notFound().build();
    }
}
