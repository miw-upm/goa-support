package es.upm.api.infrastructure.jpa.repositories;

import es.upm.api.infrastructure.jpa.entities.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
}
