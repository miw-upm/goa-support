package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.IssueDto;
import es.upm.api.domain.model.IssueListDto;
import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.UserDto;
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

import java.util.List;
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
        UserDto createdByUser = new UserDto();
        createdByUser.setId(issueId);
        createdByUser.setMobile("600000000");
        createdByUser.setEmail("ana@test.com");
        createdByUser.setFirstName("Ana");
        createdByUser.setFamilyName("Lopez");
        createdByUser.setAddress("Street 1");
        createdByUser.setCity("Madrid");
        createdByUser.setPostalCode("28001");
        createdByUser.setProvince("Madrid");
        createdByUser.setDocumentType("DNI");
        createdByUser.setIdentity("12345678A");
        createdByUser.setRole("LAWYER");
        response.setCreatedByUser(createdByUser);

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
                .andExpect(jsonPath("$.githubIssueUrl").value("https://github.com/test-owner/test-repo/issues/123"))
                .andExpect(jsonPath("$.createdByUser.id").value(issueId.toString()))
                .andExpect(jsonPath("$.createdByUser.mobile").value("600000000"))
                .andExpect(jsonPath("$.createdByUser.email").value("ana@test.com"))
                .andExpect(jsonPath("$.createdByUser.firstName").value("Ana"))
                .andExpect(jsonPath("$.createdByUser.familyName").value("Lopez"))
                .andExpect(jsonPath("$.createdByUser.address").value("Street 1"))
                .andExpect(jsonPath("$.createdByUser.city").value("Madrid"))
                .andExpect(jsonPath("$.createdByUser.postalCode").value("28001"))
                .andExpect(jsonPath("$.createdByUser.province").value("Madrid"))
                .andExpect(jsonPath("$.createdByUser.documentType").value("DNI"))
                .andExpect(jsonPath("$.createdByUser.identity").value("12345678A"))
                .andExpect(jsonPath("$.createdByUser.role").value("LAWYER"));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenReadIssueByIdDoesNotExist() throws Exception {
        UUID issueId = UUID.randomUUID();

        when(issueService.readIssueById(issueId)).thenThrow(new NotFoundException("Issue id: " + issueId));

        mockMvc.perform(get(IssueResource.ISSUES + "/{id}", issueId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldListAllIssues() throws Exception {
        IssueListDto issue1 = new IssueListDto();
        issue1.setId(UUID.randomUUID());
        issue1.setTitle("Bug Issue");
        issue1.setIssueType(Type.BUG);
        issue1.setIssueStatus(Status.IN_PROGRESS);
        issue1.setCreatedAt(java.time.LocalDateTime.now());

        IssueListDto issue2 = new IssueListDto();
        issue2.setId(UUID.randomUUID());
        issue2.setTitle("Feature Issue");
        issue2.setIssueType(Type.IMPROVEMENT);
        issue2.setIssueStatus(Status.PENDING);
        issue2.setCreatedAt(java.time.LocalDateTime.now());

        List<IssueListDto> issues = List.of(issue1, issue2);

        when(issueService.getAllIssues()).thenReturn(issues);

        mockMvc.perform(get(IssueResource.ISSUES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Bug Issue"))
                .andExpect(jsonPath("$[0].issueType").value("BUG"))
                .andExpect(jsonPath("$[0].issueStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[1].title").value("Feature Issue"))
                .andExpect(jsonPath("$[1].issueType").value("IMPROVEMENT"))
                .andExpect(jsonPath("$[1].issueStatus").value("PENDING"));
    }

    @Test
    @WithMockUser
    void shouldListEmptyIssuesWhenNoIssuesExist() throws Exception {
        when(issueService.getAllIssues()).thenReturn(List.of());

        mockMvc.perform(get(IssueResource.ISSUES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void shouldListIssuesByType() throws Exception {
        IssueListDto issue1 = new IssueListDto();
        issue1.setId(UUID.randomUUID());
        issue1.setTitle("Bug 1");
        issue1.setIssueType(Type.BUG);
        issue1.setIssueStatus(Status.PENDING);
        issue1.setCreatedAt(java.time.LocalDateTime.now());

        IssueListDto issue2 = new IssueListDto();
        issue2.setId(UUID.randomUUID());
        issue2.setTitle("Bug 2");
        issue2.setIssueType(Type.BUG);
        issue2.setIssueStatus(Status.IN_PROGRESS);
        issue2.setCreatedAt(java.time.LocalDateTime.now());

        List<IssueListDto> issues = List.of(issue1, issue2);

        when(issueService.getIssuesByType(Type.BUG)).thenReturn(issues);

        mockMvc.perform(get(IssueResource.ISSUES)
                        .param("type", "BUG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Bug 1"))
                .andExpect(jsonPath("$[0].issueType").value("BUG"))
                .andExpect(jsonPath("$[1].title").value("Bug 2"))
                .andExpect(jsonPath("$[1].issueType").value("BUG"));
    }
}
