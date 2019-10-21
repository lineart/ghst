package pro.cosy.ghst.webapp;

import graphql.schema.DataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.cosy.ghst.service.RepoServiceClient;

import java.time.LocalDate;
import java.time.Period;

@Component
public class GraphQLDataFetchers {

    @Autowired
    private RepoServiceClient serviceClient;

    public DataFetcher getRepoById() {
        return dataFetchingEnvironment -> {
            Integer repoId = dataFetchingEnvironment.getArgument("id");
            return serviceClient.getRepoById(String.valueOf(repoId));
        };
    }

    public DataFetcher getRepos() {
        return dataFetchingEnvironment -> {
            Integer page = dataFetchingEnvironment.getArgument("page");
            return serviceClient.getRepos(String.valueOf(page), null, null).getRepos();
        };
    }

    public DataFetcher getReposTrending() {
        return dataFetchingEnvironment -> {
            return serviceClient.getTrendingReposCreateAfter(LocalDate.now().minus(Period.ofDays(7)), 10).getRepos();
        };
    }

}