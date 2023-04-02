package searchengine.dto.statistics;

import lombok.Data;
import lombok.Value;

@Value
public class StatisticsResponseDto {
    private boolean result;
    private StatisticsDataDto statistics;
}
