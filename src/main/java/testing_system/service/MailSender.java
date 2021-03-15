package testing_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSender {
    @Autowired
    private JavaMailSender mailSender; // JavaMailSender - отправляет сообщения

    @Value("${spring.mail.username}")
    private String username;

    public void send(String theme, String userEmail, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage(); // SimpleMailMessage - создает простое сообщение
        mailMessage.setFrom(username);
        mailMessage.setSubject(theme);
        mailMessage.setTo(userEmail);
        mailMessage.setText(text);

        mailSender.send(mailMessage);

    }
}

