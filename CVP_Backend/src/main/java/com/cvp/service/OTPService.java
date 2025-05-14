package com.cvp.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class OTPService {
    private final Map<String, String> otpStorage = new HashMap<>(); // Key: Email, Value: OTP

    public String generateAndStoreOTP(String email) {
        String otp = OTPGenerator.generateOTP(); // Generate a 6-digit OTP
        otpStorage.put(email, otp); // Store OTP
        return otp;
    }

    public boolean verifyOTP(String email, String otp) {
        String storedOTP = otpStorage.get(email);
        return otp.equals(storedOTP); // Verify OTP
    }

    public void clearOTP(String email) {
        otpStorage.remove(email); // Clear OTP after verification
    }
}
