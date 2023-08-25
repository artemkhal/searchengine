package searchengine.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    Site findByName(String name);

    Site findByUrl(String url);

    List<Site> findByStatus(Status status);
}
