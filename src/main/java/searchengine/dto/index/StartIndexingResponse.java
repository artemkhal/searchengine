package searchengine.dto.index;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StartIndexingResponse {

    private boolean result;
    private String error;
}
