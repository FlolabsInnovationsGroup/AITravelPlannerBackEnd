package com.springboot.FlomadAIplanner.service.login;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.UserDTO;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserService userService;
    private final Converter<User, UserDetails> userConverter;
    private final Converter<UserDTO, User> userDTOToUserConverter;
    private final JwtUtil jwtUtil;
    private final UserValidator userValidator;

    public String handleOAuthLogin(UserDTO userDTO) {
        User user = userValidator.validateOAuthUserDoesNotExist(userDTO);
        if (user == null) {
            // register
            log.info("OAuth User {} Register", userDTO.getUsername());
            user = userDTOToUserConverter.convert(userDTO);
            user = userService.registerUser(user);
        } else {
            // update
            boolean isPresent = (user.getProvider()).contains(userDTO.getProvider());
            if (!isPresent) {
                String id = user.getId();
                log.info("OAuth User {} Update", userDTO.getUsername());
                if (user.getProvider() == null || user.getProvider().isEmpty()) {
                    user.setProvider(userDTO.getProvider());
                } else {
                    user.setProvider(user.getProvider() + "," + userDTO.getProvider());
                }
                user = userService.update(id, user);
            } else {
                log.info("OAuth User {} Login", userDTO.getUsername());
            }
        }
        // login
        // Authentication
        UserDetails userDetails = userConverter.convert(user);
        return jwtUtil.generateToken(userDetails);
    }
}