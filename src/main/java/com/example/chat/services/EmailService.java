package com.example.chat.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String baseUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String from,
            @Value("${app.verification-url:http://localhost:8080/users/verify}") String baseUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.baseUrl = baseUrl;
    }

    public Mono<Void> sendVerificationEmail(String email, String token) {
        if (from.isBlank()) {
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("Verify your chat account");
            message.setText(baseUrl + "?token=" + token);
            mailSender.send(message);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
