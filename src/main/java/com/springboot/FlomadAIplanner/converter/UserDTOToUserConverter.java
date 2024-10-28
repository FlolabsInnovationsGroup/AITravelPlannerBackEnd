package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.RoleService;
import com.springboot.FlomadAIplanner.service.model.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDTOToUserConverter implements Converter<UserDTO, User> {

    private final RoleService roleService;
    private final UserService userService;

    @Override
    public User convert(UserDTO source) {
        if (source == null) {
            log.info("Source DTO is 'null'");
            return null;
        }
        log.info("Converting user DTO to entity: {}", source.getUsername());

        User user = getCurrentUserIfExists(source);
        user = user == null ? new User() : user; // Create a new User if it doesn't exist

        user.setId(source.getId());
        user.setUsername(source.getUsername());
        user.setEmail(source.getEmail());
        user.setIsEmailVerified(source.getIsEmailVerified());
        user.setProvider(source.getProvider());
        user.setRoles(source.getRoles().stream().map(roleService::findByName).collect(Collectors.toSet()));
        user.setLastActive(source.getLastActive());
        return user;
    }

    private User getCurrentUserIfExists(UserDTO userDTO) {
        // Fetch the existing user to prevent overwriting sensitive data like passwords
        if (Objects.nonNull(userDTO.getId())) {
            User existingUser = userService.findById(userDTO.getId());
            if (existingUser == null) {
                log.info("User not found in the database: {}", userDTO.getId());
                return null;  // Or handle as per your application's requirement
            }
            return existingUser;
        }
        return null; // Placeholder return statement
    }
}
