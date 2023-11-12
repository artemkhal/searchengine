package searchengine.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repo.IndexRepository;
import searchengine.repo.LemmaRepository;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
public class IndexManager {

    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;


    public void calculate(Page page) throws IOException {
        if (page.getCode() < 400) {
            val map = Morphology.analyse(page);
            map.forEach((word, rank) -> save(word, rank, page));
        }
    }

    private synchronized void save(String word, int rank, Page page) {
        Lemma lemma = null;
        try {
            lemma = lemmaRepository.findByLemmaAndSiteId(word, page.getSite().getId());
        } catch (IncorrectResultSizeDataAccessException e) {
            e.printStackTrace();
        }
        if (lemma == null) {
            val newLemma = lemmaRepository.save(Lemma.builder()
                    .lemma(word)
                    .site(page.getSite())
                    .frequency(1)
                    .build());
            indexRepository.save(Index.builder()
                    .page(page)
                    .lemma(newLemma)
                    .rank(rank)
                    .build());
        } else {
            int result = lemma.getFrequency() + 1;
            lemma.setFrequency(result);
            lemmaRepository.save(lemma);
            indexRepository.save(Index.builder()
                    .page(page)
                    .lemma(lemma)
                    .rank(rank)
                    .build());
        }
    }

    public void deleteIndex(Page page) {
        val allByPage = indexRepository.findAllByPage(page.getId());
        indexRepository.deleteAll(allByPage);
    }

    public void deleteLemma(Site site) {
        val allBySiteId = lemmaRepository.findAllBySiteId(site.getId());
        lemmaRepository.deleteAll(allBySiteId);
    }


}
