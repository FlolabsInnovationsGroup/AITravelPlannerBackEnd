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

/** Abstract auth service with user/session/message copy helpers. */
@Component
public abstract class AbstractAuthService {

    private final UserService userService;
    private final SessionService sessionService;
    private final MessageService messageService;
    private final RoleService roleService;

    /**
     * Constructor.
     *
     * @param pUserService user service
     * @param pSessionService session service
     * @param pMessageService message service
     * @param pRoleService role service
     */
    public AbstractAuthService(
            final UserService pUserService,
            final SessionService pSessionService,
            final MessageService pMessageService,
            final RoleService pRoleService) {
        this.userService = pUserService;
        this.sessionService = pSessionService;
        this.messageService = pMessageService;
        this.roleService = pRoleService;
    }

    /** User service for subclasses. */
    protected final UserService getUserService() {
        return userService;
    }

    /** Session service for subclasses. */
    protected final SessionService getSessionService() {
        return sessionService;
    }

    /** Message service for subclasses. */
    protected final MessageService getMessageService() {
        return messageService;
    }

    /** Role service for subclasses. */
    protected final RoleService getRoleService() {
        return roleService;
    }

    /**
     * Creates a new user from registration DTO.
     *
     * @param userAuthDTO registration data
     * @return saved user
     */
    protected User createNewUserToRegister(final UserAuthDTO userAuthDTO) {
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

    /**
     * Copies chat history from guest user to newly registered user.
     *
     * @param newUser new user
     * @param guestUser guest user
     */
    protected void copyChatsHistoryFromGuestUserToNewUser(
            final User newUser,
            final User guestUser) {
        List<Session> guestSessions =
                sessionService.findSessionsByUserId(guestUser.getId());

        for (Session guestSession : guestSessions) {
            Session newSession = new Session();
            newSession.setUserId(newUser.getId());
            newSession.setTitle(guestSession.getTitle());
            newSession.setCreatedAt(guestSession.getCreatedAt());
            newSession.setUpdatedAt(guestSession.getUpdatedAt());
            sessionService.save(newSession);
            copyMessagesFromOldSessionToNewSession(guestSession, newSession);
        }
    }

    /**
     * Copies messages from one session to another.
     *
     * @param oldSession source session
     * @param newSession target session
     */
    protected void copyMessagesFromOldSessionToNewSession(
            final Session oldSession,
            final Session newSession) {
        messageService.getMessagesBySessionId(oldSession.getId())
                .forEach(message -> {
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
