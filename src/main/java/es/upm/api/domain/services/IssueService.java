package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.NotFoundException;
import es.upm.api.domain.model.IssueDto;
import es.upm.api.domain.persistence.IssuePersistence;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.resources.requests.CreateIssueRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

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

    public IssueDto createIssue(CreateIssueRequest issueDto) {
        UUID userId = getUserIdFromAuthentication();
        var issue = new Issue(
                issueDto.getTitle(),
                issueDto.getDescription(),
                issueDto.getTechnicalContext(),
                issueDto.getType(),
                userId
        );
        return new IssueDto(issuePersistence.create(issue));
    }

    private UUID getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return userWebClient.readUserByMobile(jwt.getSubject()).getId();
        }
        throw new NotFoundException("User not found in authentication context");
    }
}
