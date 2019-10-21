package pro.cosy.ghst.service.data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.sql.Date;
import java.util.List;

@RepositoryRestResource(collectionResourceRel = "repos", path = "repo")
public interface RepoRepository extends PagingAndSortingRepository<Repo, Long> {

    List<Repo> getAllByCreatedAtAfterOrderByStargazersCountDesc(@Param("createdAfter") Date createdAfter, Pageable page);

}