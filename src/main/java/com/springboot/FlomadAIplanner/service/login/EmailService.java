package com.springboot.FlomadAIplanner.service.login;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private final ExecutorService executorService;

    public EmailService(JavaMailSender emailSender, ExecutorService executorService) {
        this.emailSender = emailSender;
        this.executorService = executorService;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        executorService.submit(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        });
    }
}
