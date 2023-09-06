package searchengine.dto.index;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IndexPageResponse {

    private boolean result;
    private String error;
}
