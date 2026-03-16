package es.upm.api.infrastructure.resources;

import es.upm.api.infrastructure.jpa.entities.Issue;
import es.upm.api.domain.services.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(IssueResource.ISSUES)
@Tag(name = "Issues", description = "API for managing issues")
public class IssueResource {
    public static final String ISSUES = "/issues";

    private final IssueService issueService;

    public IssueResource(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    @Operation(summary = "Create a new issue", description = "Creates a new issue with the provided details")
    public ResponseEntity<Issue> createIssue(@RequestBody Issue issue) {
        Issue savedIssue = issueService.createIssue(issue);
        return ResponseEntity.ok(savedIssue);
    }
}
