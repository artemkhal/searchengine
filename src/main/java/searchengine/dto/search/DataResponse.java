package searchengine.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DataResponse {
    private String site;
    private String siteName;
    private String url;
    private String title;
    private String snippet;
    private float relevance;

}
