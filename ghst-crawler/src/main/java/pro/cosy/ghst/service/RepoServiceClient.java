package pro.cosy.ghst.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pro.cosy.ghst.data.Repo;
import pro.cosy.ghst.data.RepoList;

import java.net.ConnectException;
import java.net.URI;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static pro.cosy.ghst.Utils.toMultiMap;

public class RepoServiceClient {
    private static final Logger LOG = LoggerFactory.getLogger(RepoServiceClient.class);

    private final String serviceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public RepoServiceClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public RepoList getRepos(String page, String size, String sort) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(serviceUrl).path("/repo")
                .queryParams(toMultiMap(Map.of(
                        "page", ofNullable(page),
                        "size", ofNullable(size),
                        "sort", ofNullable(sort)
                )));

        return restTemplate.getForObject(uriBuilder.build(false).toUri(), RepoList.class);
    }

    public boolean add(Repo repo) {
        ResponseEntity<Repo> response;
        try {
            response = restTemplate.postForEntity(URI.create(serviceUrl + "/repo"), repo, Repo.class);
        } catch (HttpClientErrorException ex) {
            // Try to update
            if (ex.getStatusCode().equals(HttpStatus.CONFLICT)) {
                LOG.warn("Repo already exists, trying to update. {}", repo);
                return update(repo);
            }
            LOG.error("Failed to create repo: {}", ex.getMessage());
            return false;
        }

        if (response.getStatusCode().isError()) {
            LOG.error("Failed to create repo: {}", response.getStatusCode());
            return false;
        }

        return true;
    }

    public boolean update(Repo repo) {
        try {
            restTemplate.put(URI.create(serviceUrl + "/repo/" + repo.getId()), repo);
        } catch (HttpClientErrorException ex) {
            LOG.error("Failed to update repo: {}", ex.getMessage());
            return false;
        }
        return true;
    }

    public void serviceHealthCheck(int retries, int interval) throws InterruptedException {
        if (retries < 1) {
            throw new RuntimeException("Service health check failed! No more retries!");
        }
        // Perform basic API call
        try {
            restTemplate.headForHeaders(serviceUrl);
        } catch (ResourceAccessException ex) {
            LOG.warn("Service health check failed: {}", ex.getMessage());
            if (ex.getCause() instanceof ConnectException) {
                LOG.info("Service health check retry in {}ms, retries left: {}", interval, retries-1);
                Thread.sleep(interval);
                serviceHealthCheck(retries -1, interval);
            }
        }
    }

}
