package searchengine.dto.statistics;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SearchResultsDto {
    private boolean result;
    private int count;
    private List<StatisticsSearchDto> data;

   public SearchResultsDto(boolean result){
        this.result = result;
    }

    public SearchResultsDto(boolean result, int count, List<StatisticsSearchDto> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }
}
