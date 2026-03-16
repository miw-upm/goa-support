package es.upm.api.domain.model;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import es.upm.api.infrastructure.resources.requests.CreateIssueRequest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class IssueDto {
    private UUID id;
    private String title;
    private String description;
    private String technicalContext;
    @Enumerated(EnumType.STRING)
    private Type type;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String githubIssueId;
    private String githubIssueUrl;
    private LocalDateTime createdAt;
    private UUID createdByUserId;
    private LocalDateTime lastUpdateAt;

    public IssueDto(Issue issue)
    {
        this.id = issue.getId();
        this.title = issue.getTitle();
        this.description = issue.getDescription();
        this.technicalContext = issue.getTechnicalContext();
        this.type = issue.getType();
        this.status = issue.getStatus();
        this.githubIssueId = issue.getGithubIssueId();
        this.githubIssueUrl = issue.getGithubIssueUrl();
        this.createdAt = issue.getCreatedAt();
        this.createdByUserId = issue.getCreatedByUserId();
        this.lastUpdateAt = issue.getLastUpdateAt();
    }

    public IssueDto(CreateIssueRequest createIssueRequest) {
        this.title = createIssueRequest.title;
        this.description = createIssueRequest.description;
        this.technicalContext = createIssueRequest.technicalContext;
        this.type = createIssueRequest.type;
    }
}
