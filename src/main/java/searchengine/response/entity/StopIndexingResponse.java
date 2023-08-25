package searchengine.response.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StopIndexingResponse {

    private boolean result;
    private String error;
}
