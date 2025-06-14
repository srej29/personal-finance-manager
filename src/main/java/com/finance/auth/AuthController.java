package com.finance.auth;

import com.finance.auth.dto.AuthRequest;
import com.finance.auth.dto.AuthResponse;
import com.finance.auth.dto.LoginRequest;
import com.finance.user.User;
import com.finance.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // New import for HttpServletResponse
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext; // New import for SecurityContext
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository; // New import for SecurityContextRepository
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository; // Inject SecurityContextRepository

    public AuthController(UserService userService, AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository; // Assign injected repository
    }

    /**
     * Handles user registration.
     * @param request The AuthRequest DTO containing registration details.
     * @return ResponseEntity with success message and user ID or error message.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody AuthRequest request) {
        try {
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPassword(request.getPassword()); // Password will be encoded in UserService
            newUser.setFullName(request.getFullName());
            newUser.setPhoneNumber(request.getPhoneNumber());

            User registeredUser = userService.registerUser(newUser);
            return new ResponseEntity<>(new AuthResponse("User registered successfully", registeredUser.getId()), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new AuthResponse(e.getMessage(), null), HttpStatus.CONFLICT); // 409 Conflict for duplicate username
        } catch (Exception e) {
            // General error handler - specific exception handling in GlobalExceptionHandler
            return new ResponseEntity<>(new AuthResponse("Registration failed: " + e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles user login.
     * Authenticates the user and explicitly saves the authentication to the session.
     * @param request The LoginRequest DTO containing username and password.
     * @param httpReq The current HttpServletRequest.
     * @param httpRes The current HttpServletResponse. // New parameter for HttpServletResponse
     * @return ResponseEntity with login success message.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request,
                                                  HttpServletRequest httpReq, HttpServletResponse httpRes) { // Changed HttpSession to HttpServletRequest/Response

        // Authenticate the user using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Explicitly set the authentication object in the SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext(); // Create a new empty context
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context); // Set the populated context

        // Explicitly save the SecurityContext to the HttpSession
        // This is crucial to ensure it persists across requests.
        securityContextRepository.saveContext(context, httpReq, httpRes);

        // You can also explicitly set attributes in the session if needed (though SecurityContextRepository handles principal)
        // httpReq.getSession().setAttribute("username", authentication.getName());
        httpReq.getSession().setMaxInactiveInterval(30 * 60); // Session timeout in seconds (e.g., 30 minutes)

        return new ResponseEntity<>(new AuthResponse("Login successful", null), HttpStatus.OK);
    }

    /**
     * Handles user logout.
     * Invalidates the current HTTP session.
     * @param request The HttpServletRequest to invalidate the session.
     * @return ResponseEntity with logout success message.
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // Get existing session, don't create new
        if (session != null) {
            session.invalidate(); // Invalidate the session
        }
        // Clear security context
        SecurityContextHolder.clearContext();
        return new ResponseEntity<>(new AuthResponse("Logout successful", null), HttpStatus.OK);
    }
}
