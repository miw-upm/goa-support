package es.upm.api.infrastructure.jpa.repositories;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
    List<Issue> findByType(Type type);
    List<Issue> findByStatus(Status status);
    List<Issue> findByTypeAndStatus(Type type, Status status);
}
