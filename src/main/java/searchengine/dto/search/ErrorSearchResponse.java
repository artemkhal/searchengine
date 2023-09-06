package searchengine.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorSearchResponse {
    private boolean result;
    private String error;
}
