package com.springboot.FlomadAIplanner.service.login;

import com.springboot.FlomadAIplanner.converter.Converter;
import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import com.springboot.FlomadAIplanner.service.model.RoleService;
import com.springboot.FlomadAIplanner.service.model.SessionService;
import com.springboot.FlomadAIplanner.service.model.UserService;
import com.springboot.FlomadAIplanner.util.CurrentUserUtil;
import com.springboot.FlomadAIplanner.util.JwtUtil;
import com.springboot.FlomadAIplanner.validator.UserValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OAuth2Service extends AbstractAuthService {

    private final Converter<User, UserDetails> userConverter;
    private final Converter<UserAuthDTO, User> userDTOToUserConverter;
    private final JwtUtil jwtUtil;
    private final UserValidator userValidator;
    private final CurrentUserUtil currentUserUtil;

    public OAuth2Service(UserService userService, SessionService sessionService, MessageService messageService, RoleService roleService, PasswordEncoder passwordEncoder, Converter<User, UserDetails> userConverter, Converter<UserAuthDTO, User> userDTOToUserConverter, JwtUtil jwtUtil, UserValidator userValidator, CurrentUserUtil currentUserUtil) {
        super(userService, sessionService, messageService, roleService, passwordEncoder);
        this.userConverter = userConverter;
        this.userDTOToUserConverter = userDTOToUserConverter;
        this.jwtUtil = jwtUtil;
        this.userValidator = userValidator;
        this.currentUserUtil = currentUserUtil;
    }

    public String handleOAuthLogin(UserAuthDTO userAuthDTO) {
        User user = userValidator.validateOAuthUserDoesNotExist(userAuthDTO);

        if (user == null) {
            User existingUser = currentUserUtil.getCurrentUserFromSecurityContext();
            if (existingUser != null && userService.isGuest(existingUser)) {
                // creating user from the oAuthSystem response
                user = createNewUserToRegister(userAuthDTO);
                // copy chat history from GuestUser to new user
                copyChatsHistoryFromGuestUserToNewUser(user, existingUser);

                log.info("Upgraded guest user to registered user: {}", existingUser.getUsername());
            } else {
                log.info("OAuth User {} Register", userAuthDTO.getUsername());
                user = userDTOToUserConverter.convert(userAuthDTO);
                user = userService.registerUser(user);
            }
        } else {
            // update
            boolean isPresent = (user.getProvider()).contains(userAuthDTO.getProvider());
            if (!isPresent) {
                String id = user.getId();
                log.info("OAuth User {} Update", userAuthDTO.getUsername());
                if (user.getProvider() == null || user.getProvider().isEmpty()) {
                    user.setProvider(userAuthDTO.getProvider());
                } else {
                    user.setProvider(user.getProvider() + "," + userAuthDTO.getProvider());
                }
                user = userService.update(id, user);
            } else {
                log.info("OAuth User {} Login", userAuthDTO.getUsername());
            }
        }
        // login
        // Authentication
        UserDetails userDetails = userConverter.convert(user);
        return jwtUtil.generateToken(userDetails);
    }
}