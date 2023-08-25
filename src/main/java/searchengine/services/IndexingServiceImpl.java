package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Status;
import searchengine.repo.PageRepository;
import searchengine.repo.SiteRepository;
import searchengine.response.entity.StartIndexingResponse;
import searchengine.utils.SiteManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;


    private final SitesList sites;

    private final List<ForkJoinPool> taskList = new ArrayList<>();

    private final List<SiteManager> siteManagerList = new ArrayList<>();


    @Override
    public ResponseEntity<?> startIndexing() {
        List<searchengine.model.Site> siteList = siteRepository.findByStatus(Status.INDEXING);
        if (siteList.isEmpty()){
            ExecutorService service = Executors.newCachedThreadPool();
            for (Site site : sites.getSites()) {
                SiteManager manager = new SiteManager(pageRepository, siteRepository, site);
                siteManagerList.add(manager);

            }
            try {
                service.invokeAll(siteManagerList);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            service.shutdown();
            taskList.clear();
            siteManagerList.clear();
            return ResponseEntity.ok(StartIndexingResponse.builder().result(true).build());

        } else
            return ResponseEntity.ok(StartIndexingResponse.builder()
                .result(false)
                .error("Индексация не запущена")
                .build());

    }

    @Override
    public ResponseEntity<?> stopIndexing() {
            taskList.forEach(ForkJoinPool::shutdownNow);
            siteManagerList.forEach(SiteManager::stopIndexing);
            siteManagerList.forEach(item -> {
                item.getSite().setStatusTime(LocalDateTime.now());
                item.getSite().setLastErr("Interrupt");
                item.getSite().setStatus(Status.FAILED);
                siteRepository.save(item.getSite());
            });
            siteManagerList.forEach(SiteManager::stopIndexing);
           return null;
    }

    @Override
    public ResponseEntity<?> indexPage(String url) {
        return null;
    }


}
