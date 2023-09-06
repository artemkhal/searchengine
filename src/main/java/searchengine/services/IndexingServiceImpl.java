package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.index.StopIndexingResponse;
import searchengine.dto.search.DataResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Status;
import searchengine.repo.IndexRepository;
import searchengine.repo.LemmaRepository;
import searchengine.repo.PageRepository;
import searchengine.repo.SiteRepository;
import searchengine.dto.index.IndexPageResponse;
import searchengine.dto.index.StartIndexingResponse;
import searchengine.utils.IndexManager;
import searchengine.utils.Morphology;
import searchengine.utils.SiteManager;
import searchengine.utils.SiteParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private IndexManager indexManager;
    private final SitesList sites;

    private final List<ForkJoinPool> taskList = new ArrayList<>();

    private final List<SiteManager> siteManagerList = new ArrayList<>();


    @Override
    public ResponseEntity<?> startIndexing() {
        if (!siteRepository.findByStatus(Status.INDEXING).isEmpty()) {
            return ResponseEntity.ok(StartIndexingResponse.builder()
                    .result(false)
                    .error("Индексация уже запущена")
                    .build());
        }

        ExecutorService service = Executors.newCachedThreadPool();
        for (Site configSite : sites.getSites()) {
            if (!configSite.getUrl().substring((configSite.getUrl().length() - 1)).equals("/")) {
                configSite.setUrl(configSite.getUrl() + "/");
            }
            SiteManager manager = new SiteManager(pageRepository, siteRepository, configSite, indexManager);
            siteManagerList.add(manager);
        }
        try {
            service.invokeAll(siteManagerList);
            service.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        taskList.clear();
        siteManagerList.clear();

        return ResponseEntity.ok(StartIndexingResponse.builder().result(true).build());
    }

    @Override
    public ResponseEntity<?> stopIndexing() {
        if (siteRepository.findByStatus(Status.INDEXING).isEmpty()) {
            return ResponseEntity.ok(StopIndexingResponse.builder()
                    .result(false)
                    .error("Индексация не запущена")
                    .build());
        }
        taskList.forEach(ForkJoinPool::shutdownNow);
        siteManagerList.forEach(SiteManager::stopIndexing);
        siteManagerList.forEach(item -> {
            item.getSite().setStatusTime(LocalDateTime.now());
            item.getSite().setLastErr("Interrupt");
            item.getSite().setStatus(Status.FAILED);
            siteRepository.save(item.getSite());
        });
        siteManagerList.forEach(SiteManager::stopIndexing);
        return ResponseEntity.ok(StopIndexingResponse.builder().result(true).build());
    }

    @Override
    public ResponseEntity<?> indexPage(String url) {
        searchengine.model.Site site = findSite(url);
        if (site == null) {
            return ResponseEntity.ok(IndexPageResponse.builder().result(false).error("Данная страница находится" +
                    " за пределами сайтов, указанных в конфигурационном файле").build());
        }
        try {
            Page newPage = new SiteParser().buildPage(url);
            newPage.setSite(site);
            Page presentPage = pageRepository.findByPath(newPage.getPath());
            if (presentPage != null) {
                deletePage(presentPage);
            }
            pageRepository.save(newPage);
            if (newPage.getCode() >= 400) {
                return ResponseEntity.ok(IndexPageResponse.builder().result(true).build());
            }
            indexManager.calculate(newPage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(IndexPageResponse.builder().result(true).build());
    }

    @Override
    public List<DataResponse> search(String query, String site, String offset, String limit) throws IOException {

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

            map.forEach((page, relativeRelevance) -> dataResponseList.add(DataResponse.builder()
                    .site(page.getSite().getUrl())   //посмотреть - необходимо убрать последний слеш(возможно из за этого не переходит по ссылкам)
                    .siteName(page.getSite().getName())
                    .url(page.getPath())
                    .title(Morphology.getTitle(page))
                    .snippet(Morphology.getSnippet(sortedAnalysedQuery, page))
                    .relevance(relativeRelevance)
                    .build()));

        }
        return dataResponseList.stream().sorted(Comparator.comparing(DataResponse::getRelevance)).collect(Collectors.toList());
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



        if (lemmaList == null) {
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


    private void deletePage(Page page) {
        List<Index> indexList = indexRepository.findAllByPage(page.getId());
        List<Lemma> lemmaList = new ArrayList<>();
        for (Index index : indexList) {
            lemmaList.add(index.getLemma());
        }
        indexRepository.deleteAll(indexList);
        pageRepository.delete(page);
        lemmaRepository.deleteAll(lemmaList);
    }


    private searchengine.model.Site findSite(String url) {
        val split = url.split("/");
        String valid = "";
        searchengine.model.Site site = null;
        for (String s : split) {
            if (s.equals("http:") || s.equals("https:")) {
                valid += s + "/";
                continue;
            }
            valid += s + "/";
            log.info(valid);
            site = siteRepository.findByUrl(valid);
            if (!(site == null)) {
                break;
            }
        }
        return site;
    }
}
