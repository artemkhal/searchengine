package searchengine.services;

import searchengine.dto.search.DataResponse;

import java.io.IOException;
import java.util.List;

public interface SearchService {

    List<DataResponse> searchData(String query, String site) throws IOException;
}
