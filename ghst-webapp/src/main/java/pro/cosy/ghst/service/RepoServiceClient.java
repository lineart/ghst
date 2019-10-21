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
import java.time.LocalDate;
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

    public Repo getRepoById(String id) {
        return restTemplate.getForObject(serviceUrl + "/repo/" + id, Repo.class);
    }

    public RepoList getTrendingReposCreateAfter(LocalDate date, int size) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(serviceUrl).path("/repo/search/getAllByCreatedAtAfterOrderByStargazersCountDesc")
                .queryParam("createdAfter", date.toString())
                .queryParam("size", size);

        return restTemplate.getForObject(uriBuilder.build(false).toUri(), RepoList.class);
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
}
