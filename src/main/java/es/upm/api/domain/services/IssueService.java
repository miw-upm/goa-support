package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.IssueDto;
import es.upm.api.domain.model.IssueListDto;
import es.upm.api.domain.persistence.IssuePersistence;
import es.upm.api.domain.webclients.GitHubIssueWebClient;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class IssueService {

    private final IssuePersistence issuePersistence;
    private final UserWebClient userWebClient;
    private final GitHubIssueWebClient gitHubIssueWebClient;

    public IssueService(IssuePersistence issuePersistence,
                        UserWebClient userWebClient,
                        GitHubIssueWebClient gitHubIssueWebClient) {
        this.issuePersistence = issuePersistence;
        this.userWebClient = userWebClient;
        this.gitHubIssueWebClient = gitHubIssueWebClient;
    }

    public IssueDto createIssue(IssueDto issueDto) {
        UUID userId = getUserIdFromAuthentication();
        var issue = new Issue(
                issueDto.getTitle(),
                issueDto.getDescription(),
                issueDto.getTechnicalContext(),
                issueDto.getType(),
                userId
        );

        GitHubIssueWebClient.GitHubIssueResponse gitHubResponse = gitHubIssueWebClient.createIssue(
                issueDto.getTitle(),
                issueDto.getDescription(),
                List.of(issueDto.getType().name())
        );

        String githubIssueId = String.valueOf(gitHubResponse.number());
        String githubIssueUrl = gitHubResponse.html_url();

        issue.setGithubIssueId(githubIssueId);
        issue.setGithubIssueUrl(githubIssueUrl);

        return new IssueDto(issuePersistence.create(issue));
    }

    public IssueDto readIssueById(UUID id) {
        Issue issue = issuePersistence.readById(id);
        if (issue == null) {
            throw new NotFoundException("Issue id: " + id);
        }

        IssueDto issueDto = new IssueDto(issue);
        issueDto.setCreatedByUser(userWebClient.readUserById(issue.getCreatedByUserId()));
        return issueDto;
    }

    public IssueDto syncIssueStatus(UUID id) {
        Issue issue = issuePersistence.readById(id);
        if (issue == null) {
            throw new NotFoundException("Issue id: " + id);
        }

        if (issue.getGithubIssueId() == null && issue.getGithubIssueUrl() == null) {
            return new IssueDto(issue);
        }

        GitHubIssueWebClient.GitHubIssueState gitHubIssueState =
                gitHubIssueWebClient.readIssueState(issue.getGithubIssueId(), issue.getGithubIssueUrl());

        if (gitHubIssueState == GitHubIssueWebClient.GitHubIssueState.OPEN) {
            issuePersistence.updateStatus(issue.getId(), Status.IN_PROGRESS);
        } else if (gitHubIssueState == GitHubIssueWebClient.GitHubIssueState.CLOSED) {
            issuePersistence.updateStatus(issue.getId(), Status.FINISHED);
        }
        return new IssueDto(issue);
    }

    public List<IssueListDto> getAllIssues() {
        return issuePersistence.readAll().stream()
                .map(IssueListDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueListDto> getIssuesByType(Type type) {
        return issuePersistence.readByType(type).stream()
                .map(IssueListDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueListDto> getIssuesByStatus(Status status) {
        return issuePersistence.readByStatus(status).stream()
                .map(IssueListDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueListDto> getIssuesByTypeAndStatus(Type type, Status status) {
        return issuePersistence.readByTypeAndStatus(type, status).stream()
                .map(IssueListDto::new)
                .toList();
    }

    private UUID getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return userWebClient.readUserByMobile(jwt.getSubject()).getId();
        }
        throw new NotFoundException("User not found in authentication context");
    }
}
