package es.upm.api.domain.persistence;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IssuePersistence {
    Issue readById(UUID id);
    Issue create(Issue issue);
    void updateStatus(UUID id, Status status);
    void delete(UUID id);
}
