package es.upm.api.infrastructure.jpa.entities;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IssueTest {

    @Test
    void shouldCreateIssueUsingCustomConstructor() {

        UUID userId = UUID.randomUUID();

        Issue issue = new Issue(
                "Title",
                "Description",
                "Context",
                Type.BUG,
                userId
        );

        assertThat(issue.getTitle()).isEqualTo("Title");
        assertThat(issue.getDescription()).isEqualTo("Description");
        assertThat(issue.getTechnicalContext()).isEqualTo("Context");
        assertThat(issue.getType()).isEqualTo(Type.BUG);
        assertThat(issue.getStatus()).isEqualTo(Status.PENDING);
        assertThat(issue.getCreatedByUserId()).isEqualTo(userId);

        assertThat(issue.getCreatedAt()).isNotNull();
        assertThat(issue.getGithubIssueId()).isNull();
        assertThat(issue.getGithubIssueUrl()).isNull();
        assertThat(issue.getLastUpdateAt()).isNull();
    }

    @Test
    void shouldCreateIssueUsingBuilder() {

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Issue issue = Issue.builder()
                .id(id)
                .title("Title")
                .description("Description")
                .technicalContext("Context")
                .type(Type.BUG)
                .status(Status.PENDING)
                .createdAt(now)
                .createdByUserId(userId)
                .build();

        assertThat(issue.getId()).isEqualTo(id);
        assertThat(issue.getTitle()).isEqualTo("Title");
        assertThat(issue.getDescription()).isEqualTo("Description");
        assertThat(issue.getTechnicalContext()).isEqualTo("Context");
        assertThat(issue.getType()).isEqualTo(Type.BUG);
        assertThat(issue.getStatus()).isEqualTo(Status.PENDING);
        assertThat(issue.getCreatedAt()).isEqualTo(now);
        assertThat(issue.getCreatedByUserId()).isEqualTo(userId);
    }

    @Test
    void shouldUpdateFieldsUsingSetters() {

        Issue issue = new Issue();

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        issue.setId(id);
        issue.setTitle("Title");
        issue.setDescription("Description");
        issue.setTechnicalContext("Context");
        issue.setType(Type.BUG);
        issue.setStatus(Status.PENDING);
        issue.setCreatedAt(now);
        issue.setCreatedByUserId(userId);

        assertThat(issue.getId()).isEqualTo(id);
        assertThat(issue.getTitle()).isEqualTo("Title");
        assertThat(issue.getDescription()).isEqualTo("Description");
        assertThat(issue.getTechnicalContext()).isEqualTo("Context");
        assertThat(issue.getType()).isEqualTo(Type.BUG);
        assertThat(issue.getStatus()).isEqualTo(Status.PENDING);
        assertThat(issue.getCreatedAt()).isEqualTo(now);
        assertThat(issue.getCreatedByUserId()).isEqualTo(userId);
    }
}