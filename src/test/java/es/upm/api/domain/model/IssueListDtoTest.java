package es.upm.api.domain.model;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IssueListDtoTest {

    private List<Issue> mockIssues;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockIssues = createMockIssues();
    }

    private List<Issue> createMockIssues() {
        List<Issue> issues = new ArrayList<>();

        // Bug - Pendiente
        Issue bug1 = Issue.builder()
                .id(UUID.randomUUID())
                .title("Login button not working")
                .description("Users cannot login")
                .type(Type.BUG)
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(3))
                .createdByUserId(userId)
                .build();
        issues.add(bug1);

        // Bug - En progreso
        Issue bug2 = Issue.builder()
                .id(UUID.randomUUID())
                .title("Database connection timeout")
                .description("Connection fails after 30 seconds")
                .type(Type.BUG)
                .status(Status.IN_PROGRESS)
                .createdAt(LocalDateTime.now().minusDays(2))
                .createdByUserId(userId)
                .build();
        issues.add(bug2);

        // Mejora - Pendiente
        Issue improvement1 = Issue.builder()
                .id(UUID.randomUUID())
                .title("Add dark mode")
                .description("Implement dark mode theme")
                .type(Type.IMPROVEMENT)
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now().minusDays(1))
                .createdByUserId(userId)
                .build();
        issues.add(improvement1);

        // Mejora - Finalizada
        Issue improvement2 = Issue.builder()
                .id(UUID.randomUUID())
                .title("Optimize database queries")
                .description("Improve query performance")
                .type(Type.IMPROVEMENT)
                .status(Status.FINISHED)
                .createdAt(LocalDateTime.now())
                .createdByUserId(userId)
                .build();
        issues.add(improvement2);

        return issues;
    }

    @Test
    @DisplayName("Convertir Issue a IssueListDto correctamente")
    void testConvertIssueToIssueListDto() {
        Issue issue = mockIssues.get(0);
        IssueListDto dto = new IssueListDto(issue);

        assertEquals(issue.getId(), dto.getId());
        assertEquals(issue.getTitle(), dto.getTitle());
        assertEquals(issue.getType(), dto.getIssueType());
        assertEquals(issue.getStatus(), dto.getIssueStatus());
        assertEquals(issue.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    @DisplayName("Manejar lista vacía correctamente")
    void testEmptyList() {
        List<IssueListDto> emptyList = new ArrayList<IssueListDto>();

        assertTrue(emptyList.isEmpty());
        assertEquals(0, emptyList.size());
    }
}

