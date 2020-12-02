package main.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String username;

    public void sendMessage(String sentTo, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(sentTo);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
