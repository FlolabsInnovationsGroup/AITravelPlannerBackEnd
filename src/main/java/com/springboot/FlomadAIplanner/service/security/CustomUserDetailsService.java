package com.springboot.FlomadAIplanner.service.security;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final Converter<User, UserDetails> userConverter;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userService.findByUsernameOrEmail(identifier);
        if (user != null && user.getIsEmailVerified()) {
            return userConverter.convert(user);
        } else {
            throw new UsernameNotFoundException("User not found with username or email: " + identifier);
        }
    }

}
