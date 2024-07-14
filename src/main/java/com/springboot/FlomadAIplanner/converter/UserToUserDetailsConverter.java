package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Component
public class UserToUserDetailsConverter implements Converter<User, UserDetails> {

    @Override
    public UserDetails convert(User source) {
        if (source == null) {
            log.error("Attempted to convert a 'null' User");
            throw new IllegalArgumentException("Cannot convert null User");
        }

        log.info("Converting User [{}] to UserDetails.", source.getUsername());

        return new org.springframework.security.core.userdetails.User(
                source.getUsername(),
                source.getPassword(),
                getAuthorities(source)
        );
    }

    @SuppressWarnings("all")
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Adjust authorities based on your needs
        return Collections.emptyList();
    }
}
