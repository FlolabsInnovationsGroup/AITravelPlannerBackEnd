package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.model.entities.login.Role;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
public class UserToUserDTOConverter implements Converter<User, UserDTO> {

    @Override
    public UserDTO convert(User source) {
        if (source == null) {
            log.info("Source user is 'null'");
            return null;
        }
        log.info("Converting user entity to DTO: {}", source.getUsername());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(source.getId());
        userDTO.setUsername(source.getUsername());
        userDTO.setEmail(source.getEmail());
        userDTO.setIsEmailVerified(source.getIsEmailVerified());
        userDTO.setProvider(source.getProvider());
        userDTO.setRoles(source.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        userDTO.setLastActive(source.getLastActive());

        return userDTO;
    }
}
