package pro.cosy.ghst.github.client.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import pro.cosy.ghst.data.Repo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoSearchResult {
    @JsonProperty("items")
    private List<Repo> repos;

    private int totalCount;
    private boolean incomplete_results;

    public List<Repo> getRepos() {
        return repos;
    }

    public void setRepos(List<Repo> repos) {
        this.repos = repos;
    }
}
