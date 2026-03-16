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

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Test Title");
        issue.setDescription("Test Description");
        issue.setTechnicalContext("Test Context");
        issue.setType(Type.BUG);
        issue.setStatus(Status.PENDING);
        issue.setGithubIssueId("123");
        issue.setGithubIssueUrl("https://github.com/repo/issues/123");
        issue.setCreatedAt(now);
        issue.setCreatedByUserId(userId);
        issue.setLastUpdateAt(now);

        IssueDto dto = new IssueDto(issue);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo("Test Title");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getTechnicalContext()).isEqualTo("Test Context");
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

        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("New Issue");
        request.setDescription("New Description");
        request.setTechnicalContext("New Context");
        request.setType(Type.IMPROVEMENT);

        IssueDto dto = new IssueDto(request);

        assertThat(dto.getTitle()).isEqualTo("New Issue");
        assertThat(dto.getDescription()).isEqualTo("New Description");
        assertThat(dto.getTechnicalContext()).isEqualTo("New Context");
        assertThat(dto.getType()).isEqualTo(Type.IMPROVEMENT);

        // fields that are not in the request should remain null
        assertThat(dto.getId()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getGithubIssueId()).isNull();
        assertThat(dto.getGithubIssueUrl()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getCreatedByUserId()).isNull();
        assertThat(dto.getLastUpdateAt()).isNull();
    }

    @Test
    void shouldAllowLombokSettersAndGetters() {

        IssueDto dto = new IssueDto();

        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        dto.setId(id);
        dto.setTitle("Title");
        dto.setDescription("Desc");
        dto.setTechnicalContext("Context");
        dto.setType(Type.IMPROVEMENT);
        dto.setStatus(Status.IN_PROGRESS);
        dto.setGithubIssueId("456");
        dto.setGithubIssueUrl("https://github.com/repo/issues/456");
        dto.setCreatedAt(now);
        dto.setCreatedByUserId(UUID.randomUUID());
        dto.setLastUpdateAt(now);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getTitle()).isEqualTo("Title");
        assertThat(dto.getType()).isEqualTo(Type.IMPROVEMENT);
        assertThat(dto.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(dto.getGithubIssueId()).isEqualTo("456");
    }
}