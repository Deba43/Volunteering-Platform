package com.cvp.controller;

import jakarta.validation.Valid;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.cvp.dto.UserDto;
import com.cvp.model.User;
import com.cvp.repository.UserRepository;
import com.cvp.model.TaskSignup;
import com.cvp.service.CognitoService;
import com.cvp.service.EmailService;
import com.cvp.service.OTPService;
import com.cvp.service.UserService;
import com.cvp.service.TaskSignupService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskSignupService taskSignupService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CognitoService cognitoService;
    @Autowired
    DynamoDBMapper dynamoDBMapper;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto dto, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors());
        }

        try {
            cognitoService.signUpUser(dto.getEmail(), dto.getPassword());
            User user = userService.saveUser(dto);

            return ResponseEntity.ok(user);
        } catch (InvalidParameterException e) {
            return ResponseEntity.badRequest().body("Invalid email format");
        } catch (UsernameExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userDetails) {
        return ResponseEntity.ok(userService.updateUser(id, userDetails));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // Generate and send OTP
    @PostMapping("/generate/{email}")
    public ResponseEntity<String> generateOTP(@PathVariable String email) {
        String otp = otpService.generateAndStoreOTP(email);
        emailService.sendOTPEmail(email, otp);
        return ResponseEntity.ok("OTP sent to " + email);
    }

    // Verify OTP
    @PostMapping("/verify")
    public ResponseEntity<String> verifyOTP(@RequestParam String email, @RequestParam String otp) {
        boolean isVerified = otpService.verifyOTP(email, otp);
        if (isVerified) {
            otpService.clearOTP(email); // Clear OTP after successful verification
            return ResponseEntity.ok("OTP verified successfully!");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP.");
        }
    }

    @PostMapping("/check-password/{id}")
    public ResponseEntity<Boolean> checkPassword(
            @PathVariable String id,
            @RequestBody Map<String, String> requestBody) {
        String enteredPassword = requestBody.get("password");
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }

        User user = userOpt.get();
        boolean matches = passwordEncoder.matches(enteredPassword, user.getPassword());
        return ResponseEntity.ok(matches);
    }

    // Get Tasks by User ID
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskSignup>> getUserTasks(@PathVariable String id) {
        return ResponseEntity.ok(taskSignupService.getTasksByUserId(id));
    }
}
