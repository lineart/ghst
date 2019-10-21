package pro.cosy.ghst.github.client.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pro.cosy.ghst.data.Repo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Repos {
    private List<Repo> repos;

    public List<Repo> getRepos() {
        return repos;
    }

    public void setRepos(List<Repo> repos) {
        this.repos = repos;
    }
}
