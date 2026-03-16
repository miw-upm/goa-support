package es.upm.api.infrastructure.jpa.entities;

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
    private String technicalContext;
    @Enumerated(EnumType.ORDINAL)
    private Type type;
    @Enumerated(EnumType.ORDINAL)
    private Status status;
    private String githubIssueId;
    private String githubIssueUrl;
    private LocalDateTime createdAt;
    private UUID createdByUserId;
    private LocalDateTime lastUpdateAt;
}
