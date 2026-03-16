package es.upm.api.domain.model;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import es.upm.api.infrastructure.resources.requests.CreateIssueRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IssueDtoTest {
    @Test
    void shouldMapAllFieldsFromIssue() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Title");
        issue.setDescription("Description");
        issue.setTechnicalContext("Context");
        issue.setType(Type.BUG);
        issue.setStatus(Status.PENDING);
        issue.setGithubIssueId("123");
        issue.setGithubIssueUrl("https://github.com/repo/issues/123");
        issue.setCreatedAt(now);
        issue.setCreatedByUserId(userId);
        issue.setLastUpdateAt(now);

        // Act
        IssueDto dto = new IssueDto(issue);

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo("Title");
        assertThat(dto.getDescription()).isEqualTo("Description");
        assertThat(dto.getTechnicalContext()).isEqualTo("Context");
        assertThat(dto.getType()).isEqualTo(Type.BUG);
        assertThat(dto.getStatus()).isEqualTo(Status.PENDING);
        assertThat(dto.getGithubIssueId()).isEqualTo("123");
        assertThat(dto.getGithubIssueUrl()).isEqualTo("https://github.com/repo/issues/123");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getCreatedByUserId()).isEqualTo(userId);
        assertThat(dto.getLastUpdateAt()).isEqualTo(now);
    }

    @Test
    void shouldMapAllFieldsFromCreateIssueRequest() {
        // Arrange
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("New Issue");
        request.setDescription("New Description");
        request.setTechnicalContext("New Context");
        request.setType(Type.IMPROVEMENT);

        // Act
        IssueDto dto = new IssueDto(request);

        // Assert
        assertThat(dto.getTitle()).isEqualTo("New Issue");
        assertThat(dto.getDescription()).isEqualTo("New Description");
        assertThat(dto.getTechnicalContext()).isEqualTo("New Context");
        assertThat(dto.getType()).isEqualTo(Type.IMPROVEMENT);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getGithubIssueId()).isNull();
        assertThat(dto.getGithubIssueUrl()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getCreatedByUserId()).isNull();
        assertThat(dto.getLastUpdateAt()).isNull();
    }

    @Test
    void shouldSupportSettersAndGetters() {
        // Arrange
        IssueDto dto = new IssueDto();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UUID userId = UUID.randomUUID();

        // Act
        dto.setId(id);
        dto.setTitle("Title");
        dto.setDescription("Description");
        dto.setTechnicalContext("Context");
        dto.setType(Type.BUG);
        dto.setStatus(Status.IN_PROGRESS);
        dto.setGithubIssueId("456");
        dto.setGithubIssueUrl("https://github.com/repo/issues/456");
        dto.setCreatedAt(now);
        dto.setCreatedByUserId(userId);
        dto.setLastUpdateAt(now);

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo("Title");
        assertThat(dto.getDescription()).isEqualTo("Description");
        assertThat(dto.getTechnicalContext()).isEqualTo("Context");
        assertThat(dto.getType()).isEqualTo(Type.BUG);
        assertThat(dto.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(dto.getGithubIssueId()).isEqualTo("456");
        assertThat(dto.getGithubIssueUrl()).isEqualTo("https://github.com/repo/issues/456");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getCreatedByUserId()).isEqualTo(userId);
        assertThat(dto.getLastUpdateAt()).isEqualTo(now);
    }

    @Test
    void shouldHandleNullsGracefully() {
        IssueDto dto = new IssueDto();
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isNull();
        assertThat(dto.getTitle()).isNull();
        assertThat(dto.getType()).isNull();
    }
}