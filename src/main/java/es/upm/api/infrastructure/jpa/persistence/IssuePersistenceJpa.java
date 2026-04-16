package es.upm.api.infrastructure.jpa.persistence;

import es.upm.api.domain.persistence.IssuePersistence;
import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import es.upm.api.infrastructure.jpa.repositories.IssueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public void updateStatus(UUID id, Status status) {
        issueJpaRepository.findById(id).ifPresent(issueDb -> {
            issueDb.setStatus(status);
            issueDb.setLastUpdateAt(java.time.LocalDateTime.now());
            issueJpaRepository.save(issueDb);
        });
    }

    @Override
    public void delete(UUID id) {
        if (issueJpaRepository.existsById(id)) {
            issueJpaRepository.deleteById(id);
        }
    }

    @Override
    public List<Issue> readAll() {
        return issueJpaRepository.findAll();
    }

    @Override
    public List<Issue> readByType(Type type) {
        return issueJpaRepository.findByType(type);
    }

    @Override
    public List<Issue> readByStatus(Status status) {
        return issueJpaRepository.findByStatus(status);
    }

    @Override
    public List<Issue> readByTypeAndStatus(Type type, Status status) {
        return issueJpaRepository.findByTypeAndStatus(type,status);
    }
}
