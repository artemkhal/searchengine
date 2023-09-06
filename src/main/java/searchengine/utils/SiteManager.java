package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.*;

import searchengine.repo.PageRepository;
import searchengine.repo.SiteRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;


@Slf4j
public class SiteManager implements Callable<Boolean> {


    private final PageRepository pageRepository;


    private final SiteRepository siteRepository;

    private final Site site;

    private volatile boolean isRun;

    private final Set<String> urlList = new HashSet<>();

    private SiteParser parser;

    private final IndexManager indexManager;


    public SiteManager(PageRepository pageRepository,
                       SiteRepository siteRepository,
                       searchengine.config.Site site,
                       IndexManager indexManager) {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.site = siteConfig2Entity(site);
        this.indexManager = indexManager;
    }

    public Site getSite() {
        return site;
    }

    @Override
    public Boolean call() {
        startIndexing();
        return true;
    }

    public void startIndexing() {
        isRun = true;
        deleteRecords();
        siteRepository.save(site);
        log.info("Parsing site: " + site.getUrl());
        parser = new SiteParser(site.getUrl(), this);
        parser.invoke();

        if (isRun()) {
            site.setStatus(Status.INDEXED);
        } else {
            site.setStatus(Status.FAILED);
            site.setLastErr("Interrupt");
        }
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);

        log.info("\nЗакончена индексация сайта " + getSiteUrl() + "\n");
    }

    public synchronized void stopIndexing() {
        isRun = false;
        parser.cancel(true);
        site.setStatus(Status.FAILED);
        site.setStatusTime(LocalDateTime.now());
        site.setLastErr("Indexing for site " + site.getUrl() + " stopped by the user");
        siteRepository.save(site);
        urlList.clear();
    }

    protected void errorReport(String url, Exception exception) {
        site.setStatusTime(LocalDateTime.now());
        site.setLastErr("ERROR FROM:[" + url + "] : {" + exception.getLocalizedMessage() + "}");
        siteRepository.save(site);
    }

    protected void write2db(Page page) {
        try {
            site.setStatusTime(LocalDateTime.now());
            page.setSite(site);
            page = pageRepository.save(page);
            siteRepository.save(site);
            log.info("site : " + site.getUrl() + " , page: " + page.getPath());
            indexManager.calculate(page);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    protected String getSiteUrl() {
        return site.getUrl();
    }


    protected boolean contains(String path) {
        return urlList.contains(path);
    }

    protected void add2List(String path) {
        urlList.add(path);
    }

    protected boolean isRun() {
        return isRun;
    }

    protected synchronized boolean isValid(String url) {
        if (url.contains("?")) {
            url = url.substring(url.indexOf("?"));
        }
        List<String> list = List.of(".jpg", ".pdf", ".docx", ".doc", ".nc", ".zip", ".xls", ".png", ".eps", ".jpeg", ".xml");
        for (String str : list) {
            if (url.toLowerCase(Locale.ROOT).contains(str)) return false;
        }
        return !url.isEmpty()
                && url.startsWith(getSiteUrl())
                && !contains(url)
                && !url.contains("#")
                && isRun();
    }


    private void deleteRecords() {
        Optional.ofNullable(siteRepository.findByUrl(getSiteUrl())).ifPresent(site -> {
            List<Page> pages = pageRepository.findBySiteId(site.getId());
            pages.forEach(indexManager::deleteIndexAndLemma);
            pageRepository.deleteAll(pages);
            siteRepository.delete(site);
        });
    }


    private Site siteConfig2Entity(searchengine.config.Site site) {
        return Site.siteBuilder()
                .url(site.getUrl())
                .name(site.getName())
                .status(Status.INDEXING)
                .statusTime(LocalDateTime.now())
                .build();
    }


}
