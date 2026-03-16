package es.upm.api.domain.services;

import es.upm.api.domain.model.IssueDto;
import es.upm.api.domain.persistence.IssuePersistence;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Type;
import es.upm.api.infrastructure.resources.requests.CreateIssueRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IssueServiceIT {

    @Autowired
    private IssueService issueService;

    @Autowired
    private IssuePersistence issuePersistence;

    @MockitoBean
    private UserWebClient userWebClient;

    @AfterEach
    void cleanSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateIssueSuccessfully() {

        UUID userId = UUID.randomUUID();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(600),
                Map.of("alg", "none"),
                Map.of("sub", "mobile-user")
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(jwt, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        var userResponse = mock(es.upm.api.domain.model.UserDto.class);
        when(userResponse.getId()).thenReturn(userId);

        when(userWebClient.readUserByMobile("mobile-user"))
                .thenReturn(userResponse);

        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle("Test Issue");
        request.setDescription("Description");
        request.setTechnicalContext("Context");
        request.setType(Type.BUG);

        IssueDto result = issueService.createIssue(new IssueDto(request));

        assertThat(result).isNotNull();

        Issue persisted = issuePersistence.readById(result.getId());

        assertThat(persisted.getCreatedByUserId()).isEqualTo(userId);
        assertThat(persisted.getTitle()).isEqualTo("Test Issue");

        issuePersistence.delete(persisted.getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        org.junit.jupiter.api.Assertions.assertThrows(
                es.upm.api.domain.exceptions.NotFoundException.class,
                () -> issueService.createIssue(new IssueDto(
                        new CreateIssueRequest() {{
                            setTitle("Test");
                            setDescription("Desc");
                            setTechnicalContext("Ctx");
                            setType(Type.BUG);
                        }}
                ))
        );
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPrincipalIsNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("not-jwt", null)
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                es.upm.api.domain.exceptions.NotFoundException.class,
                () -> issueService.createIssue(new IssueDto(
                        new CreateIssueRequest() {{
                            setTitle("Test");
                            setDescription("Desc");
                            setTechnicalContext("Ctx");
                            setType(Type.BUG);
                        }}
                ))
        );
    }
}