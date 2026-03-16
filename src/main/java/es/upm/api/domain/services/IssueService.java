package es.upm.api.domain.services;

import es.upm.api.domain.persistence.IssuePersistence;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.jpa.entities.Issue;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class IssueService {

    private final IssuePersistence issuePersistence;
    private final UserWebClient userWebClient;

    public IssueService(IssuePersistence issuePersistence,
                        UserWebClient userWebClient) {
        this.issuePersistence = issuePersistence;
        this.userWebClient = userWebClient;
    }

    public Issue createIssue(Issue issue) {
        UUID userId = getUserIdFromAuthentication();
        issue.setCreatedByUserId(userId);
        issue.setCreatedAt(LocalDateTime.now());
        issue.setLastUpdateAt(LocalDateTime.now());
        return issuePersistence.create(issue);
    }

    private UUID getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return userWebClient.readUserByMobile(jwt.getSubject()).getId();
        }
        return null;
    }
}
