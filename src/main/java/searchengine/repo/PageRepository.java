package searchengine.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    Page findByPath(String path);
    List<Page> findBySiteId(int siteId);
}
