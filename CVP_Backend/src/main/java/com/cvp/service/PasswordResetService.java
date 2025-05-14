package com.cvp.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.cvp.dto.ForgotPasswordDto;
import com.cvp.dto.ResetPasswordDto;
import com.cvp.exception.InvalidEntityException;
import com.cvp.model.User;
import com.cvp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    DynamoDBMapper dynamoDBMapper;

    public String forgotPassword(ForgotPasswordDto dto) throws InvalidEntityException {
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
        if (!userOpt.isPresent()) {
            throw new InvalidEntityException("User with this email not found.");
        }
        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        dynamoDBMapper.save(user);

        String resetLink = "http://localhost:7777/users/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String text = "Hi " + user.getFirstName() + ",\n\n" +
                      "Click the link below to reset your password:\n" +
                      resetLink + "\n\n" +
                      "If you did not request this, please ignore this email.\n\n" +
                      "Best Regards,\nYour Team";
        try {
            emailService.sendEmail(user.getEmail(), subject, text);
        } catch (Exception e) {
            logger.error("Error sending password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
        return "Password reset email sent successfully.";
    }

    public String resetPassword(ResetPasswordDto dto) throws InvalidEntityException {
        Optional<User> userOpt = userRepository.findByResetToken(dto.getToken());
        if (!userOpt.isPresent()) {
            throw new InvalidEntityException("Invalid or expired password reset token.");
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setResetToken(null);
        dynamoDBMapper.save(user);
        return "Password reset successfully.";
    }
}
