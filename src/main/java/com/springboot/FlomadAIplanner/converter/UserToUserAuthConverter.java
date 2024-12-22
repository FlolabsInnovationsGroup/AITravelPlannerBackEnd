package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserToUserAuthConverter implements Converter<User, UserAuthDTO> {

    @Override
    public UserAuthDTO convert(User source) {
        if (source == null) {
            log.error("Attempted to convert a 'null' User");
            throw new IllegalArgumentException("Cannot convert null User");
        }

        log.info("Converting User [{}] to UserDTO.", source.getUsername());

        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUsername(source.getUsername());
        userAuthDTO.setPassword(source.getPassword());
        userAuthDTO.setEmail(source.getEmail());

        return userAuthDTO;
    }
}
