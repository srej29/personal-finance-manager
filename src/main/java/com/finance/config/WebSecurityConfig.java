package com.finance.config;

import com.finance.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository; // New import
import org.springframework.security.web.context.SecurityContextRepository; // New import


@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for API endpoints
                .authorizeHttpRequests(authorize -> authorize
                        // Publicly accessible endpoints: register, login, logout, and the temporary debug endpoint
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/debug/authstatus").permitAll() // TEMPORARY DEBUG ENDPOINT
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        // Return 401 Unauthorized for unauthenticated requests to protected endpoints
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                // Explicitly configure SecurityContextRepository to use HttpSession
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(httpSessionSecurityContextRepository()) // Use our custom bean
                )
                .formLogin(AbstractHttpConfigurer::disable) // Disable default form login
                .httpBasic(AbstractHttpConfigurer::disable) // Disable default HTTP Basic authentication
                .logout(AbstractHttpConfigurer::disable); // Keep this disabled to use our custom logout controller

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return username -> userService.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Defines the SecurityContextRepository to explicitly use HttpSession for storing SecurityContext.
     * @return An instance of HttpSessionSecurityContextRepository.
     */
    @Bean
    public SecurityContextRepository httpSessionSecurityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
