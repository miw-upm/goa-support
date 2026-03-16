package es.upm.api.domain.model;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class IssueDto {
    public UUID id;
    public String title;
    public String description;
    public String technicalContext;
    @Enumerated(EnumType.STRING)
    public Type type;
    @Enumerated(EnumType.STRING)
    public Status status;
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
}
