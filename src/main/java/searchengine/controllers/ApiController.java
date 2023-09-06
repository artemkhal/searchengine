package searchengine.controllers;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.dto.search.ErrorSearchResponse;
import searchengine.dto.search.SuccessSearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final IndexingService indexingService;
    private final StatisticsService statisticsService;


    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing(){
        return indexingService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing(){
        return indexingService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam(name = "url") String url){
        return indexingService.indexPage(url);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String query,
                                    @RequestParam(name = "site", required = false) String site,
                                    @RequestParam(name = "offset", required = false) String offset,
                                    @RequestParam(name = "limit", required = false) String limit){
        SuccessSearchResponse result = null;

        try {
            val search = indexingService.search(query, site, offset, limit);
            if (search == null){
                return new ResponseEntity<>(ErrorSearchResponse.builder().result(false).error("Ничего не найдено"), HttpStatus.BAD_REQUEST);
            }
            result = SuccessSearchResponse
                    .builder()
                    .result(true)
                    .count(search.size())
                    .data(search)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(result);



    }
}
