package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.IssueDto;
import es.upm.api.domain.services.IssueService;
import es.upm.api.infrastructure.resources.requests.CreateIssueRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

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
    public ResponseEntity<IssueDto> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        IssueDto savedIssue = issueService.createIssue(new IssueDto(request));
        return ResponseEntity.ok(savedIssue);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read issue by id", description = "Returns full details for the requested issue")
    public ResponseEntity<IssueDto> readIssueById(@PathVariable UUID id) {
        return ResponseEntity.ok(issueService.readIssueById(id));
    }

    @PutMapping("/{id}/sync")
    @Operation(summary = "Sync issue status with GitHub", description = "Synchronizes issue status based on associated GitHub issue state")
    public ResponseEntity<IssueDto> syncIssueStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(issueService.syncIssueStatus(id));
    }
}

