package com.finance.debug;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/authstatus")
    public ResponseEntity<String> getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok("Authentication Status: NOT Authenticated (Anonymous/Null)");
        } else {
            return ResponseEntity.ok("Authentication Status: Authenticated as " + authentication.getName() + " with roles " + authentication.getAuthorities());
        }
    }
}
