package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Email;
import es.upm.api.domain.services.EmailService;
import es.upm.miw.security.Security;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_URL_TOKEN)
@RequestMapping(EmailResource.EMAILS)
public class EmailResource {
    public static final String EMAILS = "/emails";
    public static final String SIMPLE = "/simple";
    public static final String HTML = "/html";

    private final EmailService emailService;

    public EmailResource(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping(SIMPLE)
    public void sendSimple(@Valid @RequestBody Email email) {
        this.emailService.sendSimple(email);
    }

    @PostMapping(HTML)
    public void sendHtml(@Valid @RequestBody Email email) {
        this.emailService.sendHtml(email);
    }
}
