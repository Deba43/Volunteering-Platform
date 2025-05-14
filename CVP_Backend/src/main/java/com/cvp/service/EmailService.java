package com.cvp.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendOTPEmail(String toEmail, String otp) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("Your OTP for Verification");
        mailMessage.setText("Your OTP is: " + otp + ". It will expire in 5 minutes.");

        mailSender.send(mailMessage);
    }

    public void sendEmailForTaskSignUp(String to, String taskName, String volunteerName, LocalDate signupDate) {
        String subject = "Task Registration Confirmation";

        String body = "Dear " + volunteerName + ",\n\n" +
                      "We are pleased to inform you that you have successfully registered for the task: **" + taskName + "**.\n\n" +
                      "📅 **Registration Date:** " + signupDate + "\n\n" +
                      "Thank you for your participation. If you have any questions, feel free to contact us.\n\n" +
                      "Best Regards,\n" +
                      "Community Volunteering Platform";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);

        System.out.println("Email sent successfully to " + to);
    }
    
}
