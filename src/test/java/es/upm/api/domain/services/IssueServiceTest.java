package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.IssueDto;
import es.upm.api.domain.model.UserDto;
import es.upm.api.domain.persistence.IssuePersistence;
import es.upm.api.domain.webclients.GitHubIssueWebClient;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssuePersistence issuePersistence;

    @Mock
    private UserWebClient userWebClient;

    @Mock
    private GitHubIssueWebClient gitHubIssueWebClient;

    @InjectMocks
    private IssueService issueService;

    @AfterEach
    void cleanSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateIssue() {
        UUID userId = UUID.randomUUID();
        setJwtAuthentication("mobile-user");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        when(userWebClient.readUserByMobile("mobile-user")).thenReturn(userDto);

        Issue persisted = new Issue("Title", "Desc", "Ctx", Type.BUG, userId);
        persisted.setId(UUID.randomUUID());
        when(issuePersistence.create(any(Issue.class))).thenReturn(persisted);

        IssueDto input = new IssueDto();
        input.setTitle("Title");
        input.setDescription("Desc");
        input.setTechnicalContext("Ctx");
        input.setType(Type.BUG);

        IssueDto result = issueService.createIssue(input);

        assertThat(result.getId()).isEqualTo(persisted.getId());
        assertThat(result.getCreatedByUserId()).isEqualTo(userId);
    }

    @Test
    void shouldThrowNotFoundWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> issueService.createIssue(new IssueDto()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found in authentication context");
    }

    @Test
    void shouldThrowNotFoundWhenPrincipalIsNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("plain-user", null)
        );

        assertThatThrownBy(() -> issueService.createIssue(new IssueDto()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found in authentication context");
    }

    @Test
    void shouldThrowNotFoundWhenSyncIssueDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(issuePersistence.readById(id)).thenReturn(null);

        assertThatThrownBy(() -> issueService.syncIssueStatus(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Issue id");
    }

    @Test
    void shouldSkipSyncWhenIssueHasNoGitHubAssociation() {
        UUID id = UUID.randomUUID();
        Issue issue = new Issue("Title", "Desc", "Ctx", Type.BUG, UUID.randomUUID());
        issue.setId(id);
        issue.setStatus(Status.PENDING);
        issue.setGithubIssueId(null);
        issue.setGithubIssueUrl(null);
        when(issuePersistence.readById(id)).thenReturn(issue);

        IssueDto result = issueService.syncIssueStatus(id);

        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        verify(gitHubIssueWebClient, never()).readIssueState(any(), any());
        verify(issuePersistence, never()).updateStatus(any(), any());
    }

    @Test
    void shouldSetInProgressWhenGitHubIssueIsOpen() {
        UUID id = UUID.randomUUID();
        Issue issue = new Issue("Title", "Desc", "Ctx", Type.BUG, UUID.randomUUID());
        issue.setId(id);
        issue.setStatus(Status.PENDING);
        issue.setGithubIssueId("10");
        issue.setGithubIssueUrl("https://github.com/acme/repo/issues/10");
        when(issuePersistence.readById(id)).thenReturn(issue);
        when(gitHubIssueWebClient.readIssueState("10", "https://github.com/acme/repo/issues/10"))
                .thenReturn(GitHubIssueWebClient.GitHubIssueState.OPEN);

        issueService.syncIssueStatus(id);

        verify(issuePersistence).updateStatus(id, Status.IN_PROGRESS);
    }

    @Test
    void shouldSetFinishedWhenGitHubIssueIsClosed() {
        UUID id = UUID.randomUUID();
        Issue issue = new Issue("Title", "Desc", "Ctx", Type.BUG, UUID.randomUUID());
        issue.setId(id);
        issue.setStatus(Status.IN_PROGRESS);
        issue.setGithubIssueId("11");
        issue.setGithubIssueUrl("https://github.com/acme/repo/issues/11");
        when(issuePersistence.readById(id)).thenReturn(issue);
        when(gitHubIssueWebClient.readIssueState("11", "https://github.com/acme/repo/issues/11"))
                .thenReturn(GitHubIssueWebClient.GitHubIssueState.CLOSED);

        issueService.syncIssueStatus(id);

        verify(issuePersistence).updateStatus(id, Status.FINISHED);
    }

    @Test
    void shouldNotUpdateStatusWhenGitHubStateIsNull() {
        UUID id = UUID.randomUUID();
        Issue issue = new Issue("Title", "Desc", "Ctx", Type.BUG, UUID.randomUUID());
        issue.setId(id);
        issue.setStatus(Status.PENDING);
        issue.setGithubIssueId("12");
        issue.setGithubIssueUrl("https://github.com/acme/repo/issues/12");
        when(issuePersistence.readById(id)).thenReturn(issue);
        when(gitHubIssueWebClient.readIssueState("12", "https://github.com/acme/repo/issues/12"))
                .thenReturn(null);

        IssueDto result = issueService.syncIssueStatus(id);

        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        verify(issuePersistence, never()).updateStatus(eq(id), any());
    }

    private void setJwtAuthentication(String subject) {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(600),
                Map.of("alg", "none"),
                Map.of("sub", subject)
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null)
        );
    }
}
