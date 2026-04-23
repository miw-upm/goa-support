package es.upm.api.domain.services;

import es.upm.api.domain.model.Email;
import es.upm.miw.exception.BadGatewayException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${app.mail.from}")
    private String from;

    public void sendSimple(Email email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email.getTo());
            message.setSubject(email.getSubject());
            message.setText(email.getBody());
            mailSender.send(message);
        } catch (MailException e) {
            throw new BadGatewayException("Error enviando email simple.", e);
        }
    }

    public void sendHtml(Email email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(email.getBody(), true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new BadGatewayException("Error enviando email HTML.", e);
        }
    }
}
