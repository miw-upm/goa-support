package es.upm.api.infrastructure.jpa.persistence;

import es.upm.api.domain.persistence.IssuePersistence;
import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.repositories.IssueRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class IssuePersistenceJpa implements IssuePersistence {
    private final IssueRepository issueJpaRepository;
    public IssuePersistenceJpa(IssueRepository issueJpaRepository) {
        this.issueJpaRepository = issueJpaRepository;
    }

    @Override
    public Issue readById(UUID id) {
        return issueJpaRepository.findById(id).orElse(null);
    }

    @Override
    public Issue create(Issue issue) {
        issueJpaRepository.save(issue);
        return issue;
    }

    @Override
    public void update(UUID id, Issue issue) {
        if (issueJpaRepository.existsById(id)) {
            issue.setId(id);
            issueJpaRepository.save(issue);
        }
    }

    @Override
    public void delete(UUID id) {
        if (issueJpaRepository.existsById(id)) {
            issueJpaRepository.deleteById(id);
        }
    }
}
