package com.cvp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvp.dto.ForgotPasswordDto;
import com.cvp.dto.ResetPasswordDto;
import com.cvp.exception.InvalidEntityException;
import com.cvp.service.PasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserForgotPasswordController {

    @Autowired
    private PasswordResetService passwordResetService;
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDto dto) throws InvalidEntityException {
        String response = passwordResetService.forgotPassword(dto);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto dto) throws InvalidEntityException {
        String response = passwordResetService.resetPassword(dto);
        return ResponseEntity.ok(response);
    }
}

