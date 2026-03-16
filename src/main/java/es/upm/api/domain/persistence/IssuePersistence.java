package es.upm.api.domain.persistence;

import es.upm.api.infrastructure.jpa.entities.Issue;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IssuePersistence {
    Issue readById(UUID id);
    Issue create(Issue issue);
    void update(UUID id, Issue issue);
    void delete(UUID id);
}
