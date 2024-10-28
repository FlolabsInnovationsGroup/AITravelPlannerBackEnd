package com.springboot.FlomadAIplanner.service.login;

import com.springboot.FlomadAIplanner.dto.UserAuthDTO;
import com.springboot.FlomadAIplanner.model.entities.Message;
import com.springboot.FlomadAIplanner.model.entities.Session;
import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.service.model.MessageService;
import com.springboot.FlomadAIplanner.service.model.RoleService;
import com.springboot.FlomadAIplanner.service.model.SessionService;
import com.springboot.FlomadAIplanner.service.model.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public abstract class AbstractAuthService {

    protected final UserService userService;
    protected final SessionService sessionService;
    protected final MessageService messageService;
    protected final RoleService roleService;

    public AbstractAuthService(UserService userService, SessionService sessionService, MessageService messageService, RoleService roleService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.messageService = messageService;
        this.roleService = roleService;
    }

    protected User createNewUserToRegister(UserAuthDTO userAuthDTO) {
        User user = new User();
        user.setUsername(userAuthDTO.getUsername());
        user.setPassword(userAuthDTO.getPassword());
        user.setEmail(userAuthDTO.getEmail());
        user.setIsEmailVerified(false);
        user.setProvider("none");
        user.setLastActive(LocalDateTime.now());
        user.getRoles().add(roleService.findByName("USER"));
        userService.save(user);
        return user;
    }

    protected void copyChatsHistoryFromGuestUserToNewUser(User newUser, User guestUser) {
        List<Session> guestSessions = sessionService.findSessionsByUserId(guestUser.getId());

        for (Session guestSession : guestSessions) {
            Session newSession = new Session();
            newSession.setUserId(newUser.getId());
            newSession.setTitle(guestSession.getTitle());
            newSession.setCreatedAt(guestSession.getCreatedAt());
            newSession.setUpdatedAt(guestSession.getUpdatedAt());
            sessionService.save(newSession);

            // Copy messages from old session to new session
            copyMessagesFromOldSessionToNewSession(guestSession, newSession);

        }
    }

    protected void copyMessagesFromOldSessionToNewSession(Session oldSession, Session newSession) {
        messageService.getMessagesBySessionId(oldSession.getId()).forEach(message -> {
            Message newMessage = new Message();
            newMessage.setSessionId(newSession.getId());
            newMessage.setContent(message.getContent());
            newMessage.setCreatedAt(message.getCreatedAt());
            newMessage.setMetadata(message.getMetadata());
            newMessage.setMessageType(message.getMessageType());
            messageService.save(newMessage);
        });
    }
}
