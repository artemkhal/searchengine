package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import searchengine.model.*;

import searchengine.repo.PageRepository;
import searchengine.repo.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.SimpleFormatter;


@Slf4j
public class SiteManager implements Runnable {


    private final PageRepository pageRepository;


    private final SiteRepository siteRepository;

    private final Site site;

    private static volatile boolean isRun;

    private final Set<String> urlList = new HashSet<>();

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
    public void run() {
        startIndexing();
    }

    public void startIndexing() {
        isRun = true;
        deleteRecords();
        log.info("Parsing site: " + site.getUrl());
        saveStatus(Status.INDEXING);
        SiteParser parser = new SiteParser(site.getUrl(), this);
        parser.invoke();
        saveStatus(Status.INDEXED);
        log.info("\nЗакончена индексация сайта " + getSiteUrl() + "\n");
    }

    private void saveStatus(Status status) {
        switch (status){
            case INDEXING -> {
                site.setStatus(Status.INDEXING);
                break;
            }
            case INDEXED -> {
                site.setStatus(Status.INDEXED);
                break;
            }
        }
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);

    }

    public synchronized void stopIndexing() {
        isRun = false;
        site.setStatus(Status.FAILED);
        site.setStatusTime(LocalDateTime.now());
        site.setLastErr("Indexing for site " + site.getUrl() + " stopped by the user");
        siteRepository.save(site);
        urlList.clear();
    }

    protected void errorReport(String url, Exception exception) {
        site.setStatusTime(LocalDateTime.now());
        String message = "ERROR FROM:[" + url + "] : {" + exception.getLocalizedMessage() + "}";
        site.setLastErr(message);
        siteRepository.save(site);
        log.info(message);
    }

    protected void write2db(Page page) {
        try {
            site.setStatusTime(LocalDateTime.now());
            page.setSite(site);
            pageRepository.save(page);
            log.info("Save page for site " + page.getSite().getName());
            indexManager.calculate(page);
        } catch (DataIntegrityViolationException | IOException e) {
            e.getMessage();
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
            log.info("Deleting old site data " + site.getUrl());
            List<Page> pages = pageRepository.findBySiteId(site.getId());
            pages.forEach(indexManager::deleteIndex);
            indexManager.deleteLemma(site);
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
