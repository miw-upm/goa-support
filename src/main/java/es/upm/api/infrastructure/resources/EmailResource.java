package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Email;
import es.upm.api.domain.services.EmailService;
import es.upm.miw.security.Security;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_URL_TOKEN)
@RequestMapping(EmailResource.EMAILS)
@RequiredArgsConstructor
public class EmailResource {
    public static final String EMAILS = "/emails";
    public static final String SIMPLE = "/simple";
    public static final String HTML = "/html";
    public static final String ATTACHMENT = "/attachment";

    private final EmailService emailService;

    @PostMapping(SIMPLE)
    public void sendSimple(@Valid @RequestBody Email email) {
        this.emailService.sendSimple(email);
    }

    @PostMapping(HTML)
    public void sendHtml(@Valid @RequestBody Email email) {
        this.emailService.sendHtml(email);
    }

    @PostMapping(value = HTML + ATTACHMENT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void sendHtmlWithAttachment(@Valid @RequestPart("email") Email email,
                                       @RequestPart("attachment") MultipartFile attachment) {
        this.emailService.sendHtml(email, attachment);
    }
}
