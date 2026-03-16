package es.upm.api.infrastructure.jpa.persistence;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import es.upm.api.infrastructure.jpa.repositories.IssueRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IssuePersistenceJpaIT {

    @Autowired
    private IssuePersistenceJpa issuePersistence;

    @Autowired
    private IssueRepository issueRepository;

    @Test
    void shouldCreateAndReadIssue() {

        UUID userId = UUID.randomUUID();

        Issue issue = new Issue(
                "Title",
                "Description",
                "Context",
                Type.BUG,
                userId
        );

        Issue created = issuePersistence.create(issue);

        Issue found = issuePersistence.readById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Title");
        assertThat(found.getCreatedByUserId()).isEqualTo(userId);
    }

    @Test
    void shouldUpdateStatusIssue() {

        UUID userId = UUID.randomUUID();

        Issue issue = new Issue(
                "Old Title",
                "Description",
                "Context",
                Type.BUG,
                userId
        );

        Issue created = issueRepository.save(issue);

        issuePersistence.updateStatus(created.getId(), es.upm.api.infrastructure.jpa.entities.Status.IN_PROGRESS);

        Issue found = issueRepository.findById(created.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(es.upm.api.infrastructure.jpa.entities.Status.IN_PROGRESS);
    }

    @Test
    void shouldDeleteIssue() {

        UUID userId = UUID.randomUUID();

        Issue issue = new Issue(
                "Title",
                "Description",
                "Context",
                Type.BUG,
                userId
        );

        Issue created = issueRepository.save(issue);

        issuePersistence.delete(created.getId());

        boolean exists = issueRepository.existsById(created.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void shouldNotUpdateStatusNonExistentIssue() {
        UUID nonExistentId = UUID.randomUUID();

        issuePersistence.updateStatus(nonExistentId, Status.PENDING);

        boolean exists = issueRepository.existsById(nonExistentId);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldNotDeleteNonExistentIssue() {
        UUID nonExistentId = UUID.randomUUID();

        issuePersistence.delete(nonExistentId);

        boolean exists = issueRepository.existsById(nonExistentId);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldPersistLongDescriptionAndTechnicalContext() {
        UUID userId = UUID.randomUUID();
        String longDescription = "A".repeat(5000);
        String longTechnicalContext = "B".repeat(6000);

        Issue issue = new Issue(
                "Title",
                longDescription,
                longTechnicalContext,
                Type.BUG,
                userId
        );

        Issue created = issuePersistence.create(issue);
        Issue found = issuePersistence.readById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getDescription()).isEqualTo(longDescription);
        assertThat(found.getTechnicalContext()).isEqualTo(longTechnicalContext);
    }
}