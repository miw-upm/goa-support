package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.IssueDto;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.services.IssueService;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import es.upm.api.infrastructure.resources.requests.CreateIssueRequest;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueResourceFT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IssueService issueService;

    @Test
    @WithMockUser
    void shouldCreateIssue() throws Exception {

        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Test Issue");
        request.setDescription("Description");
        request.setTechnicalContext("Context");
        request.setType(Type.BUG);

        UUID issueId = UUID.randomUUID();

        IssueDto response = new IssueDto();
        response.setId(issueId);
        response.setTitle("Test Issue");
        response.setDescription("Description");
        response.setTechnicalContext("Context");
        response.setType(Type.BUG);

        when(issueService.createIssue(new IssueDto(request))).thenReturn(response);

        mockMvc.perform(post(IssueResource.ISSUES)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId.toString()))
                .andExpect(jsonPath("$.title").value("Test Issue"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.technicalContext").value("Context"))
                .andExpect(jsonPath("$.type").value("BUG"));
    }

    @Test
    @WithMockUser
    void shouldSyncIssueStatus() throws Exception {
        UUID issueId = UUID.randomUUID();

        IssueDto response = new IssueDto();
        response.setId(issueId);
        response.setStatus(Status.FINISHED);

        when(issueService.syncIssueStatus(issueId)).thenReturn(response);

        mockMvc.perform(put(IssueResource.ISSUES + "/{id}/sync", issueId)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId.toString()))
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    @Test
    @WithMockUser
    void shouldReadIssueById() throws Exception {
        UUID issueId = UUID.randomUUID();

        IssueDto response = new IssueDto();
        response.setId(issueId);
        response.setTitle("Issue");
        response.setDescription("Description");
        response.setTechnicalContext("Context");
        response.setType(Type.BUG);
        response.setStatus(Status.PENDING);
        response.setGithubIssueId("123");
        response.setGithubIssueUrl("https://github.com/test-owner/test-repo/issues/123");

        when(issueService.readIssueById(issueId)).thenReturn(response);

        mockMvc.perform(get(IssueResource.ISSUES + "/{id}", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId.toString()))
                .andExpect(jsonPath("$.title").value("Issue"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.technicalContext").value("Context"))
                .andExpect(jsonPath("$.type").value("BUG"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.githubIssueId").value("123"))
                .andExpect(jsonPath("$.githubIssueUrl").value("https://github.com/test-owner/test-repo/issues/123"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenReadIssueByIdDoesNotExist() throws Exception {
        UUID issueId = UUID.randomUUID();

        when(issueService.readIssueById(issueId)).thenThrow(new NotFoundException("Issue id: " + issueId));

        mockMvc.perform(get(IssueResource.ISSUES + "/{id}", issueId))
                .andExpect(status().isNotFound());
    }
}
