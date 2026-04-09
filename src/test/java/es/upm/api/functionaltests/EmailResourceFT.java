package es.upm.api.functionaltests;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.Email;
import es.upm.api.domain.services.EmailService;
import es.upm.api.infrastructure.resources.EmailResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailResourceFT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @Test
    @WithMockUser
    void shouldSendSimpleEmail() throws Exception {
        Email email = Email.builder().to("to@example.com").subject("Asunto").body("Cuerpo").build();

        mockMvc.perform(post(EmailResource.EMAILS + EmailResource.SIMPLE)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isOk());

        verify(emailService).sendSimple(email);
    }

    @Test
    @WithMockUser
    void shouldSendHtmlEmail() throws Exception {
        Email email = Email.builder().to("to@example.com").subject("Asunto").body("Cuerpo").build();

        mockMvc.perform(post(EmailResource.EMAILS + EmailResource.HTML)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isOk());

        verify(emailService).sendHtml(email);
    }
}
