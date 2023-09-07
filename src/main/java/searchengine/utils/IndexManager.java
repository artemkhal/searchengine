package searchengine.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repo.IndexRepository;
import searchengine.repo.LemmaRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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

    private void save(String word, int rank, Page page) {
        val lemma = lemmaRepository.findByLemmaAndSiteId(word, page.getSite().getId());
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

    public void deleteIndexAndLemma(Page page) {
        val allByPage = indexRepository.findAllByPage(page.getId());
        for (Index index : allByPage) {
            val lemma = index.getLemma();
            val frequency = lemma.getFrequency();
            if (frequency > 1){
                lemma.setFrequency(frequency - 1);
                lemmaRepository.save(lemma);
            } else lemmaRepository.delete(lemma);
        }

    }


}
