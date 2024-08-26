package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserRevertConverter implements Converter<UserDTO, User> {

    @Override
    public User convert(UserDTO source) {
        if (source == null) {
            log.error("Attempted to convert a 'null' UserDTO");
            throw new IllegalArgumentException("Cannot convert null UserDTO");
        }

        log.info("Converting UserDTO [{}] to User.", source.getUsername());

        User user = new User();
        user.setUsername(source.getUsername());
        user.setPassword(source.getPassword());
        user.setEmail(source.getEmail());
        user.setProvider(source.getProvider());

        return user;
    }
}
