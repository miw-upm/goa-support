package es.upm.api.domain.model;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para visualizar incidencias en modo lista.
 * Contiene solo los campos necesarios para la visualización: title, issueType, issueStatus y created_at
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueListDto {
    private UUID id;
    private String title;
    private Type issueType;
    private Status issueStatus;
    private LocalDateTime createdAt;

    public IssueListDto(Issue issue) {
        this.id = issue.getId();
        this.title = issue.getTitle();
        this.issueType = issue.getType();
        this.issueStatus = issue.getStatus();
        this.createdAt = issue.getCreatedAt();
    }
}