package com.springboot.FlomadAIplanner.config.login;

import com.springboot.FlomadAIplanner.filter.JwtRequestFilter;
import com.springboot.FlomadAIplanner.filter.UserLastActiveUpdateFilter;
import com.springboot.FlomadAIplanner.handler.login.CustomAuthenticationFailureHandler;
import com.springboot.FlomadAIplanner.handler.login.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final UserLastActiveUpdateFilter userLastActiveUpdateFilter;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public WebSecurityConfig(UserDetailsService userDetailsService,
                             JwtRequestFilter jwtRequestFilter, UserLastActiveUpdateFilter userLastActiveUpdateFilter,
                             PasswordEncoder passwordEncoder,
                             CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                             CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.userLastActiveUpdateFilter = userLastActiveUpdateFilter;
        this.passwordEncoder = passwordEncoder;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    private static final List<String> PUBLIC_URLS = List.of(
            "/api/audio/transcribe",
            "/api/auth/signup",
            "/api/auth/signin",
            "/api/auth/verify",
            "/api/auth/guest-token",
            "/api/chat/call",
            "/api/chat/stream",
            "/api/chat/call/entity",
            "/api/locations/**",
            "/users/current",
            "/sessions/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/ws/**"
    );

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder); // Use the injected PasswordEncoder

        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                            "https://www.flomadtravel.com", // ✅ Allow production frontend
                            "http://localhost:3000"         // ✅ Keep localhost for development
                    )); // ✅ Allow frontend
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_URLS.toArray(new String[0])).permitAll() // ✅ Now uses the list
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "actuator/health",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/chat", "/api/stream").hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization.baseUri("/oauth2/authorization"))
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(userLastActiveUpdateFilter, JwtRequestFilter.class);

        return http.build();
    }
}
