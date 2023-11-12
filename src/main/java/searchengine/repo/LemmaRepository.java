package searchengine.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;

import java.util.List;

public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    Lemma findByLemmaAndSiteId(String lemma, int siteId);

    @Query(value = "SELECT COUNT(*) FROM `lemma` WHERE `site_id` = :site_id", nativeQuery = true)
    int lemmaCountBySite(@Param("site_id") int siteId);

    List<Lemma> findByLemma(String lemma);

    List<Lemma> findAllBySiteId(int siteId);
}

