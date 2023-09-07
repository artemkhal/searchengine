package searchengine.services;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DataResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repo.IndexRepository;
import searchengine.repo.LemmaRepository;
import searchengine.repo.SiteRepository;
import searchengine.utils.Morphology;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService{

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private LemmaRepository lemmaRepository;

    private final int MAX_SNIPPET_LENGTH = 300;


    @Override
    public List<DataResponse> searchData(String query, String site) throws IOException {
        val analysedQuery = Morphology.analyse(query);
        val sortedAnalysedQuery = new ArrayList<>(analysedQuery.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .toList());
        if (sortedAnalysedQuery.isEmpty()){
            return null;
        }
        val firstLemma = sortedAnalysedQuery.iterator().next().getKey();

        val pages = findPages(firstLemma, site);
        for (Map.Entry<String, Integer> entry : sortedAnalysedQuery) {
            val iterator = pages.iterator();
            while (iterator.hasNext()) {
                HashMap<String, Integer> map = Morphology.analyse(iterator.next());
                if (!map.containsKey(entry.getKey()))
                    iterator.remove();
            }
        }
        List<Lemma> lemmaList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedAnalysedQuery) {
            lemmaList.addAll(lemmaRepository.findByLemma(entry.getKey()));
        }
        Map<Page, Float> map = getAbsRelevanceMap(pages, lemmaList);
        List<DataResponse> dataResponseList = new ArrayList<>();
        if (!map.isEmpty()) {
            val maxRAbs = Collections.max(map.values());
            map.forEach((k, v) -> map.put(k, v / maxRAbs));
            map.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());

            map.forEach((page, relativeRelevance) -> {
                String snippet = Morphology.getSnippet(sortedAnalysedQuery, page);
                if (snippet.length() > MAX_SNIPPET_LENGTH)
                    snippet = snippet.substring(0, MAX_SNIPPET_LENGTH);
                dataResponseList.add(DataResponse.builder()
                        .site(page.getSite().getUrl())
                        .siteName(page.getSite().getName())
                        .url(page.getPath())
                        .title(Morphology.getTitle(page))
                        .snippet(snippet)
                        .relevance(relativeRelevance)
                        .build());
            });
        }
        if (!dataResponseList.isEmpty())
            dataResponseList.sort(new DataResponseComparator<>());
        return dataResponseList;
    }


    private Map<Page, Float> getAbsRelevanceMap(List<Page> pages, List<Lemma> lemmaList) {
        HashMap<Page, Float> map = new HashMap<>();
        for (Page page : pages) {
            for (Lemma lemma : lemmaList) {
                val index = indexRepository.findAllByLemmaAndPage(lemma.getId(), page.getId());
                if (index == null){
                    continue;
                }
                if (map.containsKey(page)) {
                    float rAbs = map.get(page) + index.getRank();
                    map.put(page, rAbs);
                } else {
                    map.put(page, index.getRank());
                }
            }
        }
        return map;
    }


    private List<Page> findPages(String word, String site) {
        searchengine.model.Site repositorySite = null;
        List<Lemma> lemmaList = new ArrayList<>();
        if (site != null){
            repositorySite = siteRepository.findByUrl(site);
        }
        if (repositorySite != null){
            val lemma = lemmaRepository.findByLemmaWereSiteId(word, repositorySite.getId());
            if (lemma != null){
                lemmaList.add(lemma);
            }
        } else lemmaList.addAll(lemmaRepository.findByLemma(word));

        if (lemmaList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Index> indexList = new ArrayList<>();

        for (Lemma lemma1 : lemmaList) {
            indexList.addAll( indexRepository.findAllByLemma(lemma1.getId()));
        }
        return indexList.stream()
                .map(Index::getPage)
                .collect(Collectors.toList());
    }

    private static class DataResponseComparator<T extends Comparable<?>> implements Comparator<DataResponse>{
        @Override
        public int compare(DataResponse o1, DataResponse o2) {
            return (int)(o2.getRelevance() * 10 - o1.getRelevance() * 10);
        }
    }
}
