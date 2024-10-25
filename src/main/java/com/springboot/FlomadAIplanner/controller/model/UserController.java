package com.springboot.FlomadAIplanner.controller.model;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.util.CurrentUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserUtil currentUserUtil;

    public UserController(UserService userService, CurrentUserUtil currentUserUtil) {
        this.userService = userService;
        this.currentUserUtil = currentUserUtil;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("Creating new user: {}", user);
        User savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        log.info("Retrieving user by id: {}", id);
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<Iterable<User>> getAllUsers() {
        log.info("Retrieving all users");
        Iterable<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        log.info("Updating user with id: {}", id);
        User user = userService.update(id, updatedUser);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("Deleting user with id: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser() {
        User user = currentUserUtil.getCurrentUserFromSecurityContext();
        if (user != null) {
            log.info("Retrieving current user: {}", user.getUsername());
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
}
