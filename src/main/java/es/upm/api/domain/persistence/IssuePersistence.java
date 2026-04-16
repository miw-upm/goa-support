package es.upm.api.domain.persistence;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssuePersistence {
    Issue readById(UUID id);
    Issue create(Issue issue);
    void updateStatus(UUID id, Status status);
    void delete(UUID id);
    List<Issue> readAll();
    List<Issue> readByType(Type type);
}
