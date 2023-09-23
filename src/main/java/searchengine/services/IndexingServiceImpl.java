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
            val site = item.getSite();
            site.setStatusTime(LocalDateTime.now());
            site.setLastErr("Interrupt");
            site.setStatus(Status.FAILED);
            siteRepository.save(item.getSite());
        });
        siteManagerList.forEach(SiteManager::stopIndexing);
        return ResponseEntity.ok(StopIndexingResponse.builder().result(true).build());
    }

    @Override
    public ResponseEntity<?> indexPage(String url) {
        final int BAD_STATUS_CODE = 400;
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
            if (newPage.getCode() >= BAD_STATUS_CODE) {
                return ResponseEntity.ok(IndexPageResponse.builder().result(true).build());
            }
            indexManager.calculate(newPage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(IndexPageResponse.builder().result(true).build());
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
            site = siteRepository.findByUrl(valid);
            if (!(site == null)) {
                break;
            }
        }
        return site;
    }
}
