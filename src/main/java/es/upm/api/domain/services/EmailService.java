package es.upm.api.domain.services;

import es.upm.api.domain.model.Email;
import es.upm.miw.exception.BadGatewayException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

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

    public void sendHtml(Email email, MultipartFile attachment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(email.getBody(), true);

            if (attachment != null && !attachment.isEmpty()) {
                helper.addAttachment(
                        Objects.requireNonNull(attachment.getOriginalFilename()),
                        new ByteArrayResource(attachment.getBytes()),
                        "application/pdf"
                );
            }
            mailSender.send(message);
        } catch (MessagingException | IOException | MailException e) {
            throw new BadGatewayException("Error enviando email HTML.", e);
        }
    }
}
