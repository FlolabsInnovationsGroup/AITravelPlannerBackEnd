package com.springboot.FlomadAIplanner.config;

import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableWebSecurity
public class WebSecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection
//                .authorizeHttpRequests(authz -> authz
//                        // Permit access without authentication to specific endpoints
//                        .requestMatchers("/api/v1/places/nearby").permitAll()
//                        .requestMatchers("/planTrip").permitAll() // Allow access to /planTrip without authentication
//                        .requestMatchers("/flight-offers").permitAll() // Allow anonymous access to /flight-offers
//                        .requestMatchers("/login").permitAll() // Allow access to /login without authentication
//                        .requestMatchers("/error").permitAll() // Allow access to /error without authentication
//                        .requestMatchers("/convert").permitAll()
//                        .anyRequest().authenticated()) // All other requests need authentication
//                .formLogin(form -> form
//                        .loginPage("/login") // Custom login page
//                        .permitAll()) // Allow access to the login page without authentication
//                .logout(LogoutConfigurer::permitAll); // Allow access to the logout functionality without authentication
//        return http.build();
//    }

}
