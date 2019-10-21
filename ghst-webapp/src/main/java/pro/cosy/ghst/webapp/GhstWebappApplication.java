package pro.cosy.ghst.webapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pro.cosy.ghst.service.RepoServiceClient;

@SpringBootApplication
public class GhstWebappApplication {

	@Value("${repo.service.url}")
	private String SERVICE_URL;

	private RepoServiceClient serviceClient;

	public static void main(String[] args) {
		SpringApplication.run(GhstWebappApplication.class, args);
	}

	@Bean
	public RepoServiceClient repoServiceClient() {
		if (serviceClient == null) {
			serviceClient = new RepoServiceClient(SERVICE_URL);
		}
		return serviceClient;
	}
}
