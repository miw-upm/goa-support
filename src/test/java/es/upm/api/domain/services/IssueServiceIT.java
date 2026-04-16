package es.upm.api.domain.services;

import es.upm.api.domain.model.IssueDto;
import es.upm.api.domain.model.IssueListDto;
import es.upm.api.domain.persistence.IssuePersistence;
import es.upm.api.domain.webclients.GitHubIssueWebClient;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import es.upm.api.infrastructure.resources.requests.CreateIssueRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IssueServiceIT {

    @Autowired
    private IssueService issueService;

    @Autowired
    private IssuePersistence issuePersistence;

    @MockitoBean
    private UserWebClient userWebClient;

    @MockitoBean
    private GitHubIssueWebClient gitHubIssueWebClient;

    @AfterEach
    void cleanSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateIssueSuccessfully() {

        UUID userId = UUID.randomUUID();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(600),
                Map.of("alg", "none"),
                Map.of("sub", "mobile-user")
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(jwt, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        var userResponse = mock(es.upm.api.domain.model.UserDto.class);
        when(userResponse.getId()).thenReturn(userId);

        when(userWebClient.readUserByMobile("mobile-user"))
                .thenReturn(userResponse);

        when(gitHubIssueWebClient.createIssue(anyString(), anyString(), anyList()))
                .thenReturn(new GitHubIssueWebClient.GitHubIssueResponse("open", "https://github.com/test-owner/test-repo/issues/123", 123));

        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Test Issue");
        request.setDescription("Description");
        request.setTechnicalContext("Context");
        request.setType(Type.BUG);

        IssueDto result = issueService.createIssue(new IssueDto(request));

        assertThat(result).isNotNull();

        Issue persisted = issuePersistence.readById(result.getId());

        assertThat(persisted.getCreatedByUserId()).isEqualTo(userId);
        assertThat(persisted.getTitle()).isEqualTo("Test Issue");
        assertThat(persisted.getGithubIssueId()).isEqualTo("123");
        assertThat(persisted.getGithubIssueUrl()).isEqualTo("https://github.com/test-owner/test-repo/issues/123");

        issuePersistence.delete(persisted.getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        org.junit.jupiter.api.Assertions.assertThrows(
                es.upm.api.domain.exceptions.NotFoundException.class,
                () -> issueService.createIssue(new IssueDto(
                        new CreateIssueRequest() {{
                            setTitle("Test");
                            setDescription("Desc");
                            setTechnicalContext("Ctx");
                            setType(Type.BUG);
                        }}
                ))
        );
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPrincipalIsNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not-jwt", null)
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                es.upm.api.domain.exceptions.NotFoundException.class,
                () -> issueService.createIssue(new IssueDto(
                        new CreateIssueRequest() {{
                            setTitle("Test");
                            setDescription("Desc");
                            setTechnicalContext("Ctx");
                            setType(Type.BUG);
                        }}
                ))
        );
    }

    @Test
    void shouldReadIssueByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        Issue issue = new Issue("Issue", "Description", "Context", Type.BUG, userId);
        issue.setGithubIssueId("123");
        issue.setGithubIssueUrl("https://github.com/test-owner/test-repo/issues/123");
        issue = issuePersistence.create(issue);

        var userResponse = mock(es.upm.api.domain.model.UserDto.class);
        when(userResponse.getId()).thenReturn(userId);
        when(userResponse.getFirstName()).thenReturn("Ana");
        when(userResponse.getFamilyName()).thenReturn("Lopez");
        when(userResponse.getMobile()).thenReturn("600000000");
        when(userResponse.getEmail()).thenReturn("ana@test.com");
        when(userResponse.getAddress()).thenReturn("Street 1");
        when(userResponse.getCity()).thenReturn("Madrid");
        when(userResponse.getPostalCode()).thenReturn("28001");
        when(userResponse.getProvince()).thenReturn("Madrid");
        when(userResponse.getDocumentType()).thenReturn("DNI");
        when(userResponse.getIdentity()).thenReturn("12345678A");
        when(userResponse.getRole()).thenReturn("LAWYER");
        when(userWebClient.readUserById(userId)).thenReturn(userResponse);

        IssueDto result = issueService.readIssueById(issue.getId());

        assertThat(result.getId()).isEqualTo(issue.getId());
        assertThat(result.getTitle()).isEqualTo("Issue");
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getTechnicalContext()).isEqualTo("Context");
        assertThat(result.getType()).isEqualTo(Type.BUG);
        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        assertThat(result.getGithubIssueId()).isEqualTo("123");
        assertThat(result.getGithubIssueUrl()).isEqualTo("https://github.com/test-owner/test-repo/issues/123");
        assertThat(result.getCreatedByUser()).isNotNull();
        assertThat(result.getCreatedByUser().getId()).isEqualTo(userId);
        assertThat(result.getCreatedByUser().getFirstName()).isEqualTo("Ana");
        assertThat(result.getCreatedByUser().getFamilyName()).isEqualTo("Lopez");
        assertThat(result.getCreatedByUser().getMobile()).isEqualTo("600000000");
        assertThat(result.getCreatedByUser().getEmail()).isEqualTo("ana@test.com");
        assertThat(result.getCreatedByUser().getAddress()).isEqualTo("Street 1");
        assertThat(result.getCreatedByUser().getCity()).isEqualTo("Madrid");
        assertThat(result.getCreatedByUser().getPostalCode()).isEqualTo("28001");
        assertThat(result.getCreatedByUser().getProvince()).isEqualTo("Madrid");
        assertThat(result.getCreatedByUser().getDocumentType()).isEqualTo("DNI");
        assertThat(result.getCreatedByUser().getIdentity()).isEqualTo("12345678A");
        assertThat(result.getCreatedByUser().getRole()).isEqualTo("LAWYER");

        issuePersistence.delete(issue.getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenReadIssueByIdDoesNotExist() {
        assertThrows(
                es.upm.api.domain.exceptions.NotFoundException.class,
                () -> issueService.readIssueById(UUID.randomUUID())
        );
    }

    @Test
    void shouldSyncIssueToFinishedWhenGitHubIssueIsClosed() {
        UUID userId = UUID.randomUUID();
        Issue issue = new Issue("Issue", "Description", "Context", Type.BUG, userId);
        issue.setGithubIssueId("15");
        issue.setGithubIssueUrl("https://github.com/test-owner/test-repo/issues/15");
        issue = issuePersistence.create(issue);

        when(gitHubIssueWebClient.readIssueState("15", "https://github.com/test-owner/test-repo/issues/15"))
                .thenReturn(GitHubIssueWebClient.GitHubIssueState.CLOSED);

        IssueDto result = issueService.syncIssueStatus(issue.getId());

        assertThat(result.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(issuePersistence.readById(issue.getId()).getStatus()).isEqualTo(Status.FINISHED);

        issuePersistence.delete(issue.getId());
    }

    @Test
    void shouldKeepIssueStatusWhenGitHubIssueIsOpen() {
        UUID userId = UUID.randomUUID();
        Issue issue = new Issue("Issue", "Description", "Context", Type.BUG, userId);
        issue.setStatus(Status.IN_PROGRESS);
        issue.setGithubIssueId("16");
        issue.setGithubIssueUrl("https://github.com/test-owner/test-repo/issues/16");
        issue = issuePersistence.create(issue);

        when(gitHubIssueWebClient.readIssueState("16", "https://github.com/test-owner/test-repo/issues/16"))
                .thenReturn(GitHubIssueWebClient.GitHubIssueState.OPEN);

        IssueDto result = issueService.syncIssueStatus(issue.getId());

        assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(issuePersistence.readById(issue.getId()).getStatus()).isEqualTo(Status.IN_PROGRESS);

        issuePersistence.delete(issue.getId());
    }

    @Test
    void shouldSkipSynchronizationWhenIssueHasNoGitHubAssociation() {
        UUID userId = UUID.randomUUID();
        Issue issue = new Issue("Issue", "Description", "Context", Type.BUG, userId);
        issue.setStatus(Status.PENDING);
        issue = issuePersistence.create(issue);

        IssueDto result = issueService.syncIssueStatus(issue.getId());

        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        verifyNoInteractions(gitHubIssueWebClient);

        issuePersistence.delete(issue.getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenSyncIssueDoesNotExist() {
        assertThrows(
                es.upm.api.domain.exceptions.NotFoundException.class,
                () -> issueService.syncIssueStatus(UUID.randomUUID())
        );
    }

    @Test
    void shouldNotUpdateStatusWhenGitHubStateIsNull() {
        UUID userId = UUID.randomUUID();
        Issue issue = new Issue("Issue", "Description", "Context", Type.BUG, userId);
        issue.setStatus(Status.PENDING);
        issue.setGithubIssueId("17");
        issue.setGithubIssueUrl("https://github.com/test-owner/test-repo/issues/17");
        issue = issuePersistence.create(issue);

        when(gitHubIssueWebClient.readIssueState("17", "https://github.com/test-owner/test-repo/issues/17"))
                .thenReturn(null);

        IssueDto result = issueService.syncIssueStatus(issue.getId());

        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        assertThat(issuePersistence.readById(issue.getId()).getStatus()).isEqualTo(Status.PENDING);

        issuePersistence.delete(issue.getId());
    }

    @Test
    void shouldReturnAllIssuesAsIssueListDto() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        Issue issue1 = new Issue("Issue 1", "Description 1", "Context 1", Type.BUG, userId1);
        Issue createdIssue1 = issuePersistence.create(issue1);

        Issue issue2 = new Issue("Issue 2", "Description 2", "Context 2", Type.IMPROVEMENT, userId2);
        issue2.setStatus(Status.IN_PROGRESS);
        Issue createdIssue2 = issuePersistence.create(issue2);

        // When
        List<IssueListDto> result = issueService.getAllIssues();

        // Then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2); // Since there might be other issues from previous tests

        // Find the created issues in the result
        IssueListDto dto1 = result.stream()
                .filter(dto -> dto.getId().equals(createdIssue1.getId()))
                .findFirst().orElseThrow();
        assertThat(dto1.getTitle()).isEqualTo("Issue 1");
        assertThat(dto1.getIssueType()).isEqualTo(Type.BUG);
        assertThat(dto1.getIssueStatus()).isEqualTo(Status.PENDING);

        IssueListDto dto2 = result.stream()
                .filter(dto -> dto.getId().equals(createdIssue2.getId()))
                .findFirst().orElseThrow();
        assertThat(dto2.getTitle()).isEqualTo("Issue 2");
        assertThat(dto2.getIssueType()).isEqualTo(Type.IMPROVEMENT);
        assertThat(dto2.getIssueStatus()).isEqualTo(Status.IN_PROGRESS);

        // Cleanup
        issuePersistence.delete(createdIssue1.getId());
        issuePersistence.delete(createdIssue2.getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoIssues() {
        // When
        List<IssueListDto> result = issueService.getAllIssues();
        assertThat(result).isNotNull();
    }
}
