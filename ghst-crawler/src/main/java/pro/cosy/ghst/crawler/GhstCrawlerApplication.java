package pro.cosy.ghst.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import pro.cosy.ghst.data.Repo;
import pro.cosy.ghst.github.client.GitHubClient;
import pro.cosy.ghst.service.RepoServiceClient;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@SpringBootApplication
public class GhstCrawlerApplication implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(GhstCrawlerApplication.class);
    private static final String H1 = "===== {} =====";

    @Value("${repo.service.url}")
    private String SERVICE_URL;
    @Value("${repo.service.check.retries}")
    private int service_check_retries;
    @Value("${repo.service.check.interval}")
    private int service_check_interval;
    @Value("${repo.age.days}")
    private int repoAgeDays;

    @Value("${github.api.url}")
    private String GH_URL;

    private RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
		SpringApplication.run(GhstCrawlerApplication.class, args);
	}

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOG.info("Service URL: {}", SERVICE_URL);
        LOG.info("GitHub API URL: {}", GH_URL);

        var repoSrv = new RepoServiceClient(SERVICE_URL);
        // Waiting for Repo Storage service to start
        repoSrv.serviceHealthCheck(service_check_retries, service_check_interval);

        var gitHubClient = new GitHubClient(GH_URL);

        LOG.info(H1, "Gathering Repos started");

        // Searching for GH repos created last week
        var date = LocalDate.now().minus(Period.ofDays(repoAgeDays)).toString();
        LOG.info("Searching for repos created after {}", date);
        // Query example: q=created:>2019-10-21
        gitHubClient.searchRepo("created%3A%3E" + date, new RepoListener() {
            // Saving to storage service
            @Override
            public void onNew(Repo repo) {
                repoSrv.add(repo);
            }

            @Override
            public void onList(List<Repo> repoList) {
                LOG.info("Got {} repos", repoList.size());
                repoList.forEach(this::onNew);
            }
        });

        LOG.info(H1, "Gathering Repos completed");
    }

}
