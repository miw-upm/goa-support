package es.upm.api.domain.webclients;

import es.upm.api.domain.exceptions.BadGatewayException;
import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GitHubIssueWebClientIT {

    @Test
    void shouldReadOpenStateFromGitHubUrl() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "", "");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.github.com/repos/acme/support/issues/15"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-GitHub-Api-Version", "2022-11-28"))
                .andRespond(withSuccess("{\"state\":\"open\"}", MediaType.APPLICATION_JSON));

        GitHubIssueWebClient.GitHubIssueState result =
                client.readIssueState(null, "https://github.com/acme/support/issues/15");

        assertThat(result).isEqualTo(GitHubIssueWebClient.GitHubIssueState.OPEN);
        server.verify();
    }

    @Test
    void shouldReadClosedStateFromIssueIdAndRepositoryConfig() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "acme", "support");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.github.com/repos/acme/support/issues/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"state\":\"closed\"}", MediaType.APPLICATION_JSON));

        GitHubIssueWebClient.GitHubIssueState result = client.readIssueState("99", null);

        assertThat(result).isEqualTo(GitHubIssueWebClient.GitHubIssueState.CLOSED);
        server.verify();
    }

    @Test
    void shouldSendBearerTokenWhenConfigured() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "token-123", "acme", "support");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.github.com/repos/acme/support/issues/7"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-123"))
                .andRespond(withSuccess("{\"state\":\"open\"}", MediaType.APPLICATION_JSON));

        client.readIssueState("7", null);

        server.verify();
    }

    @Test
    void shouldThrowBadRequestWhenGithubUrlFormatIsInvalid() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "", "");

        assertThatThrownBy(() -> client.readIssueState(null, "https://example.com/not-github/issue/1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid GitHub issue URL format");
    }

    @Test
    void shouldThrowBadRequestWhenIssueDataCannotResolveEndpoint() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "", "");

        assertThatThrownBy(() -> client.readIssueState(null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot resolve GitHub issue endpoint");
    }

    @Test
    void shouldThrowNotFoundWhenGithubIssueDoesNotExist() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "acme", "support");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.github.com/repos/acme/support/issues/404"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.readIssueState("404", null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("GitHub issue not found");

        server.verify();
    }

    @Test
    void shouldThrowBadGatewayWhenGitHubReturnsNoBody() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "acme", "support");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.github.com/repos/acme/support/issues/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withNoContent());

        assertThatThrownBy(() -> client.readIssueState("1", null))
                .isInstanceOf(BadGatewayException.class)
                .hasMessageContaining("GitHub response does not include issue state");

        server.verify();
    }

    @Test
    void shouldThrowBadGatewayWhenGitHubReturnsBlankState() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "acme", "support");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.github.com/repos/acme/support/issues/2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"state\":\"\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.readIssueState("2", null))
                .isInstanceOf(BadGatewayException.class)
                .hasMessageContaining("GitHub response does not include issue state");

        server.verify();
    }

    @Test
    void shouldThrowBadGatewayWhenStateIsUnsupported() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "acme", "support");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.github.com/repos/acme/support/issues/3"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"state\":\"in_progress\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.readIssueState("3", null))
                .isInstanceOf(BadGatewayException.class)
                .hasMessageContaining("Unsupported GitHub issue state");

        server.verify();
    }

    @Test
    void shouldThrowBadGatewayWhenGitHubCallFails() {
        GitHubIssueWebClient client = new GitHubIssueWebClient("https://api.github.com", "", "acme", "support");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("https://api.github.com/repos/acme/support/issues/500"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.readIssueState("500", null))
                .isInstanceOf(BadGatewayException.class)
                .hasMessageContaining("Error querying GitHub issue state");

        server.verify();
    }

    @Test
    void shouldMapStateCaseInsensitively() {
        assertThat(GitHubIssueWebClient.GitHubIssueState.fromGitHubState("OPEN"))
                .isEqualTo(GitHubIssueWebClient.GitHubIssueState.OPEN);
        assertThat(GitHubIssueWebClient.GitHubIssueState.fromGitHubState("cLoSeD"))
                .isEqualTo(GitHubIssueWebClient.GitHubIssueState.CLOSED);
    }
}
