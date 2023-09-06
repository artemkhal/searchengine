package searchengine.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import searchengine.model.Index;

import java.util.List;

public interface IndexRepository extends CrudRepository<Index, Integer> {

    @Query(value = "SELECT * FROM `index` i WHERE i.page_id = :page_id", nativeQuery = true)
    List<Index> findAllByPage(@Param("page_id") int pageId);

    @Query(value = "SELECT * FROM `index` i WHERE i.lemma_id = :lemma_id", nativeQuery = true)
    List<Index> findAllByLemma(@Param("lemma_id") int lemmaId);

    @Query(value = "SELECT * FROM `INDEX` i WHERE i.lemma_id = :lemma_id AND i.page_id = :page_id", nativeQuery = true)
    Index findAllByLemmaAndPage(@Param("lemma_id") int lemmaId, @Param("page_id") int pageId);
}
