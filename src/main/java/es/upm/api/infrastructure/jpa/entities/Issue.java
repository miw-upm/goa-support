package es.upm.api.infrastructure.jpa.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "issues")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    private String description;
    @Column(name = "technicalContext")
    private String technicalContext;
    @Enumerated(EnumType.ORDINAL)
    private Type type;
    @Enumerated(EnumType.ORDINAL)
    private Status status;
    @Nullable
    @Column(name = "githubIssueId")
    private String githubIssueId;
    @Nullable
    @Column(name = "githubIssueUrl")
    private String githubIssueUrl;
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
    @Column(name = "createdByUserId")
    private UUID createdByUserId;
    @Nullable
    @Column(name = "lastUpdateAt")
    private LocalDateTime lastUpdateAt;

    public Issue(String title, String description, String technicalContext, Type type, Status status, UUID createdByUserId) {
        this.title = title;
        this.description = description;
        this.technicalContext = technicalContext;
        this.type = type;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.createdByUserId = createdByUserId;
        this.githubIssueId = null;
        this.githubIssueUrl = null;
        this.lastUpdateAt = null;
    }
}
