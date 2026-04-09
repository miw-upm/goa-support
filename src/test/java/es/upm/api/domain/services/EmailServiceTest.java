package es.upm.api.domain.services;

import es.upm.api.domain.model.Email;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "from", "noreply@tudominio.com");
    }

    @Test
    void testSendSimple() {
        emailService.sendSimple(Email.builder().to("to@example.com").subject("Asunto").body("Cuerpo").build());

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendSimpleVerifyContent() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSimple(Email.builder().to("to@example.com").subject("Asunto").body("Cuerpo").build());

        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("to@example.com");
        assertThat(sent.getSubject()).isEqualTo("Asunto");
        assertThat(sent.getText()).isEqualTo("Cuerpo");
        assertThat(sent.getFrom()).isEqualTo("noreply@tudominio.com");
    }

    @Test
    void testSendHtml() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendHtml(Email.builder().to("to@example.com").subject("Asunto HTML").body("<h1>Hola</h1>").build());

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendSimpleThrowsException() {
        doThrow(new MailSendException("Error SMTP"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        Email email = Email.builder().to("to@example.com").subject("Asunto").body("Cuerpo").build();

        assertThatThrownBy(() -> emailService.sendSimple(email))
                .isInstanceOf(MailSendException.class)
                .hasMessageContaining("Error SMTP");
    }
}