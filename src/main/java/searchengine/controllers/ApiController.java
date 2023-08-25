package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

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
        indexingService.startIndexing();
        return ResponseEntity.ok("Start indexing");
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing(){
        return indexingService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam String url){
        return indexingService.indexPage(url);
    }
}
