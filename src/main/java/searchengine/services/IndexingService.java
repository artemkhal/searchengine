package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.search.DataResponse;

import java.io.IOException;
import java.util.List;

public interface IndexingService {

    ResponseEntity<?> startIndexing();

    ResponseEntity<?> stopIndexing();

    ResponseEntity<?> indexPage(String url);

    List<DataResponse> search(String query, String site, String offset, String limit) throws IOException;
}
