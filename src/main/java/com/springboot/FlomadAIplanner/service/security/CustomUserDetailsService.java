package com.springboot.FlomadAIplanner.service.security;

import com.springboot.FlomadAIplanner.model.entities.login.User;
import com.springboot.FlomadAIplanner.model.entities.login.Role;
import com.springboot.FlomadAIplanner.service.model.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userService.findByUsernameOrEmail(identifier);
        if (user != null && user.getIsEmailVerified()) {
            String password = userService.isGuest(user) ? null : user.getPassword();
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                password,
                mapRolesToAuthorities(user.getRoles())); // Ensure roles are mapped
        } else {
            throw new UsernameNotFoundException("User not found with username or email: " + identifier);
        }        
    }
    /**
     * Maps roles to authorities.
     *
     * @param roles the roles to map to authorities
     * @return the authorities mapped from roles
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())) // Ensure role names are prefixed with ROLE_
                .collect(Collectors.toList());
    } 

}
