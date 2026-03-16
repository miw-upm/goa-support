package es.upm.api.infrastructure.jpa.persistence;

import es.upm.api.infrastructure.jpa.entities.Issue;
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
class IssuePersistenceJpaTest {

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
    void shouldUpdateIssue() {

        UUID userId = UUID.randomUUID();

        Issue issue = new Issue(
                "Old Title",
                "Description",
                "Context",
                Type.BUG,
                userId
        );

        Issue created = issueRepository.save(issue);

        Issue updated = new Issue(
                "New Title",
                "New Description",
                "New Context",
                Type.BUG,
                userId
        );

        issuePersistence.update(created.getId(), updated);

        Issue found = issueRepository.findById(created.getId()).orElseThrow();

        assertThat(found.getTitle()).isEqualTo("New Title");
        assertThat(found.getDescription()).isEqualTo("New Description");
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
}