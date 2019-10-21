package pro.cosy.ghst.github.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.cosy.ghst.crawler.RepoListener;
import pro.cosy.ghst.github.client.data.RepoSearchResult;

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static pro.cosy.ghst.Utils.toMultiMap;

/**
 * GitHub API Client
 */
public class GitHubClient {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubClient.class);

    private static final String URI_REPO_SEARCH = "/search/repositories";
    private static final String URI_REPO_LIST = "/repositories";

    private static final String HEADER_LINK = "Link";
    private static final long RATE_LIMIT_INTERVAL = 60_000; // ms

    private final String BASE_URL;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * @param baseUrl GitHub API Base URL
     */
    public GitHubClient(String baseUrl) {
        this.BASE_URL = baseUrl;
    }

    /**
     * Search for GitHub repositories
     * @param q Query string
     * @param sort Sorting results criteria
     * @param order Order (asc|desc)
     * @param repoListener Repos Destination
     */
    public void searchRepo(String q, String sort, String order, RepoListener repoListener) {
        // Prepare URI
        var uri = UriComponentsBuilder.fromUriString(BASE_URL).path(URI_REPO_SEARCH)
                .queryParams(toMultiMap(Map.of(
                        "q", ofNullable(q),
                        "sort", ofNullable(sort),
                        "order", ofNullable(order)))
                ).build(false).toUriString();

        // Paginated calls with intermediate results publishing
        do {
            var result = get(URI.create(uri), RepoSearchResult.class);
            if (result.hasBody()) {
                // Publish obtained repos
                repoListener.onList(result.getBody().getRepos());
            }
            uri = getNextPageUrl(result.getHeaders());
        } while (uri != null);
    }

    /**
     * Shortcut to {@link #searchRepo(String, String, String, RepoListener)}
     */
    public void searchRepo(String q, RepoListener repoListener) {
        searchRepo(q, null, null, repoListener);
    }

    /**
     * Perform GET requests with rate limits handling
     *
     * @param uri to call
     * @param cls expected return type
     */
    private <T> HttpEntity<T> get(URI uri, Class<T> cls) {
        HttpEntity<T> resp;
        try {
            resp = restTemplate.getForEntity(uri, cls);
        } catch (HttpClientErrorException ex) {
            // Wait and retry on Rate Limits hit
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)
                    && "0".equals(ex.getResponseHeaders().getFirst("X-RateLimit-Remaining"))) {
                // Figure out how long to sleep
                long sleep = getRateLimitResetInt(ex.getResponseHeaders());
                LOG.info("Hit Rate Limit, sleeping for {}ms", sleep);
                try {
                    // Restful service, indeed
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    LOG.info("Sleep interrupted: {}", e.getMessage());
                }
                // Try again
                return get(uri, cls);
            }
            LOG.error(ex.getResponseBodyAsString());
            throw ex;
        }

        return resp;
    }

    /**
     * Returns time till next Rate Limit reset (in ms)
     *
     * @param responseHeaders
     * @return time till RL reset in ms
     */
    private long getRateLimitResetInt(HttpHeaders responseHeaders) {
        long currDate = responseHeaders.getFirstDate("Date");
        long resetDate = Long.parseLong(responseHeaders.getFirst("X-RateLimit-Reset")) * 1000;

        long restTime = resetDate - currDate;

        return restTime > 1 && restTime < RATE_LIMIT_INTERVAL ? restTime : RATE_LIMIT_INTERVAL;
    }

    /**
     * Check if response has Next Page URL and return it, otherwise return null
     *
     * @param headers To check
     * @return Next page URL string, or null if not available
     */
    private String getNextPageUrl(HttpHeaders headers) {
        // Examples of Link Headers
        // Link: <https://host/path?q=a&page=2>; rel="next"
        // Link: <https://host/path?q=a&page=2>; rel="last"
        var link = headers.getFirst(HEADER_LINK);

        if (link != null) {
            final Pattern nextPage = Pattern.compile(String.format(".*<(%s.*)>;\\srel=\"next\".*", Pattern.quote(BASE_URL)));
            Matcher matcher = nextPage.matcher(link);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
