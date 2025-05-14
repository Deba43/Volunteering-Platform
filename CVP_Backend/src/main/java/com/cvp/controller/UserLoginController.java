package com.cvp.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cvp.model.Login;
import com.cvp.model.User;
import com.cvp.service.CognitoService;
import com.cvp.service.UserService;

@RestController
@RequestMapping("/users")
public class UserLoginController {

    @Autowired
    private CognitoService cognitoService;
    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Login login) {
        // Check if email exists
        if (!userService.isUserEmail(login.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Email not found."));
        }

        // Authenticate with Cognito and get token
        String token = cognitoService.login(login.getEmail(), login.getPassword());

        // Fetch user from database using EMAIL (to get the ID)
        User user = userService.getUserByEmail(login.getEmail());

        // Return token + database user ID (NOT email or Cognito ID)
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId().toString()); // Return the database-generated ID
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirmUser(@RequestParam String email,
            @RequestParam String otp) {
        cognitoService.confirmUser(email, otp);
        return ResponseEntity.ok("User confirmed successfully");
    }
}
