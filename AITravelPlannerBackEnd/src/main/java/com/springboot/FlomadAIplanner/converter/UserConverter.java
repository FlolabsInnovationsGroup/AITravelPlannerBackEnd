package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserConverter implements Converter<User, UserDTO> {

    @Override
    public UserDTO convert(User source) {
        if (source == null) {
            log.error("Attempted to convert a 'null' User");
            throw new IllegalArgumentException("Cannot convert null User");
        }

        log.info("Converting User [{}] to UserDTO.", source.getUsername());

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(source.getUsername());
        userDTO.setPassword(source.getPassword());
        userDTO.setEmail(source.getEmail());

        return userDTO;
    }
}
