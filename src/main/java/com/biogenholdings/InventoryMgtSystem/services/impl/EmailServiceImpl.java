package com.biogenholdings.InventoryMgtSystem.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl {
    private final JavaMailSender mailSender;

    public void sendEmployeeCredentials(String toEmail, String tempPassword) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Account Credentials");
        message.setText(
                "Welcome!\n\n" +
                        "Username: " + toEmail + "\n" +
                        "Temporary Password: " + tempPassword + "\n\n" +
                        "Please change your password after first login."
        );

        mailSender.send(message);
    }
}
