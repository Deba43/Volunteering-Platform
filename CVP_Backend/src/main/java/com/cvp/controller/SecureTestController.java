package com.cvp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecureTestController {

    @GetMapping("/secure-endpoint")
    public String secureEndpoint(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email"); 
        return "Access granted to: " + email;
    }
}
