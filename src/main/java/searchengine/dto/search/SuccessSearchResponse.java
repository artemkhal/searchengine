package searchengine.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SuccessSearchResponse {
    private boolean result;
    private int count;
    private List<DataResponse> data;

    @Override
    public String toString() {
        return "SuccessSearchResponse{" +
                "result=" + result +
                ", count=" + count +
                ", dataResponseList=" + data +
                '}';
    }
}
