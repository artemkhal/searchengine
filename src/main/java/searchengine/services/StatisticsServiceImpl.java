package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.repo.LemmaRepository;
import searchengine.repo.PageRepository;
import searchengine.repo.SiteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    PageRepository pageRepository;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    LemmaRepository lemmaRepository;

    private final Random random = new Random();
    private final SitesList sites;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = getPageCount(site);
            int lemmas = getLemmasCount(site);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(getStatus(site));
            item.setError(getLastError(site));
            item.setStatusTime(getStatusTime(site));
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private long getStatusTime(Site site) {
        val byUrl = siteRepository.findByUrl(site.getUrl());
        if (byUrl != null && byUrl.getStatusTime() != null){
            return convert2Millis(byUrl.getStatusTime());
        }
        log.info(LocalDateTime.now() + ": Site not found");
        return convert2Millis(LocalDateTime.now());
    }

    private long convert2Millis(LocalDateTime dateTime) {
        val zonedDateTime = dateTime.atZone(ZoneId.of("Europe/Moscow"));
        return zonedDateTime.toInstant().toEpochMilli();
    }

    private String getLastError(Site site) {
        val byUrl = siteRepository.findByUrl(site.getUrl());
        if (byUrl != null && byUrl.getLastErr() != null) {
            return byUrl.getLastErr();
        }
        return "";
    }

    private String getStatus(Site site) {
        val byUrl = siteRepository.findByUrl(site.getUrl());
        if (byUrl != null) {
            return byUrl.getStatus().toString();
        }
        return "";
    }

    private int getLemmasCount(Site site) {
        val byUrl = siteRepository.findByUrl(site.getUrl());
        if (byUrl != null) {
            return lemmaRepository.lemmaCountBySite(byUrl.getId());
        }
        return 0;
    }

    private int getPageCount(Site site) {
        val byUrl = siteRepository.findByUrl(site.getUrl());
        if (byUrl != null) {
            return byUrl.getPages().size();
        }
        return 0;

    }
}
