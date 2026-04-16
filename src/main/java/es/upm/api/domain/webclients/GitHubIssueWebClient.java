package es.upm.api.domain.webclients;

import es.upm.api.domain.exceptions.BadGatewayException;
import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Component
public class GitHubIssueWebClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String token;
    private final String repositoryOwner;
    private final String repositoryName;

    public GitHubIssueWebClient(@Value("${github.api-url:https://api.github.com}") String apiUrl,
                                @Value("${github.token:}") String token,
                                @Value("${github.repository-owner:}") String repositoryOwner,
                                @Value("${github.repository-name:}") String repositoryName) {
        this.restTemplate = new RestTemplate();
        this.apiUrl = apiUrl;
        this.token = token;
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
    }

    public GitHubIssueState readIssueState(String githubIssueId, String githubIssueUrl) {
        String endpoint = this.resolveIssueEndpoint(githubIssueId, githubIssueUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add("X-GitHub-Api-Version", "2022-11-28");
        if (StringUtils.hasText(this.token)) {
            headers.setBearerAuth(this.token);
        }

        try {
            ResponseEntity<GitHubIssueResponse> response = this.restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GitHubIssueResponse.class
            );

            if (response.getBody() == null || !StringUtils.hasText(response.getBody().state())) {
                throw new BadGatewayException("GitHub response does not include issue state");
            }

            return GitHubIssueState.fromGitHubState(response.getBody().state());
        } catch (HttpClientErrorException.NotFound exception) {
            throw new NotFoundException("GitHub issue not found");
        } catch (RestClientException exception) {
            throw new BadGatewayException("Error querying GitHub issue state");
        }
    }

    private String resolveIssueEndpoint(String githubIssueId, String githubIssueUrl) {
        if (StringUtils.hasText(githubIssueUrl)) {
            GitHubIssueLocation issueLocation = this.parseGitHubIssueUrl(githubIssueUrl.trim());
            if (issueLocation != null) {
                return this.apiUrl + "/repos/" + issueLocation.owner() + "/" + issueLocation.repository() + "/issues/" + issueLocation.issueNumber();
            }
            throw new BadRequestException("Invalid GitHub issue URL format");
        }

        if (StringUtils.hasText(githubIssueId)
                && StringUtils.hasText(this.repositoryOwner)
                && StringUtils.hasText(this.repositoryName)) {
            return this.apiUrl + "/repos/" + this.repositoryOwner + "/" + this.repositoryName + "/issues/" + githubIssueId;
        }

        throw new BadRequestException("Cannot resolve GitHub issue endpoint from issue data");
    }

    private GitHubIssueLocation parseGitHubIssueUrl(String githubIssueUrl) {
        try {
            URI uri = URI.create(githubIssueUrl);
            String host = uri.getHost();
            if (!StringUtils.hasText(host) || !"github.com".equalsIgnoreCase(host)) {
                return null;
            }

            String path = uri.getPath();
            if (!StringUtils.hasText(path)) {
                return null;
            }

            String[] segments = path.split("/");
            if (segments.length < 5 || !"issues".equals(segments[3])) {
                return null;
            }

            String issueNumber = segments[4];
            if (!issueNumber.chars().allMatch(Character::isDigit)) {
                return null;
            }

            return new GitHubIssueLocation(segments[1], segments[2], issueNumber);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public record GitHubIssueResponse(String state, String html_url, int number) {
    }

    private record GitHubIssueLocation(String owner, String repository, String issueNumber) {
    }

    public enum GitHubIssueState {
        OPEN,
        CLOSED;

        public static GitHubIssueState fromGitHubState(String gitHubState) {
            return switch (gitHubState.toLowerCase()) {
                case "open" -> OPEN;
                case "closed" -> CLOSED;
                default -> throw new BadGatewayException("Unsupported GitHub issue state: " + gitHubState);
            };
        }
    }

    private record CreateGitHubIssueRequest(String title, String body, List<String> labels) {
    }
    /**
     * Creates a new GitHub issue in the configured repository.
     * @param title The issue title (required)
     * @param body The issue body/description (optional)
     * @param labels The list of labels (optional)
     * @return GitHubIssueResponse with state, html_url, and number
     */
    public GitHubIssueResponse createIssue(String title, String body, List<String> labels) {
        if (!StringUtils.hasText(this.repositoryOwner) || !StringUtils.hasText(this.repositoryName)) {
            throw new BadRequestException("Repository owner and name must be set to create an issue");
        }
        if (!StringUtils.hasText(title)) {
            throw new BadRequestException("Issue title must not be empty");
        }

        String endpoint = this.apiUrl + "/repos/" + this.repositoryOwner + "/" + this.repositoryName + "/issues";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-GitHub-Api-Version", "2022-11-28");
        if (StringUtils.hasText(this.token)) {
            headers.setBearerAuth(this.token);
        } else {
            throw new BadRequestException("GitHub token must be provided to create an issue");
        }

        CreateGitHubIssueRequest request = new CreateGitHubIssueRequest(title, body, labels);

        try {
            ResponseEntity<GitHubIssueResponse> response = this.restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    GitHubIssueResponse.class
            );

            if (response.getBody() == null) {
                throw new BadGatewayException("GitHub response does not include created issue data");
            }

            return response.getBody();
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new BadRequestException("Invalid request to GitHub: " + exception.getMessage());
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new BadGatewayException("GitHub API access forbidden: " + exception.getMessage());
        } catch (RestClientException exception) {
            throw new BadGatewayException("Error creating GitHub issue");
        }
    }
}
