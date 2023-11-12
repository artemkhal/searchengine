package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.dto.search.DataResponse;
import searchengine.dto.search.ErrorSearchResponse;
import searchengine.dto.search.SuccessSearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final IndexingService indexingService;
    private final StatisticsService statisticsService;
    private final SearchService searchService;


    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        return indexingService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        return indexingService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam(name = "url") String url) {
        return indexingService.indexPage(url);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String query,
                                    @RequestParam(name = "site", required = false) String site,
                                    @RequestParam(name = "offset", required = false) int offset,
                                    @RequestParam(name = "limit", required = false) int limit) {
        SuccessSearchResponse result = null;
        try {
            List<DataResponse> dataResponseList = searchService.searchData(query, site);
            int size = dataResponseList.size();
            int issuanceCount = limit + offset;
            if (limit < dataResponseList.size()) {
                size = dataResponseList.size();
                if (issuanceCount > size) {
                    issuanceCount = size;
                }
                dataResponseList = dataResponseList.subList(offset, issuanceCount);
            }
            if (dataResponseList.isEmpty()) {
                return new ResponseEntity<>(ErrorSearchResponse.builder().result(false).error("Ничего не найдено"), HttpStatus.BAD_REQUEST);
            }
            result = SuccessSearchResponse
                    .builder()
                    .result(true)
                    .count(size)
                    .data(dataResponseList)
                    .build();
        } catch (IOException e) {
            log.info(LocalDateTime.now() + ": Ошибка при поиске релевантных страниц по запросу \"" + query + "\"");
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(result);
    }
}
