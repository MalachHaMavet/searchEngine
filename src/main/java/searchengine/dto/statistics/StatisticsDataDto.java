package searchengine.dto.statistics;

import lombok.Value;

import java.util.List;

@Value
public class StatisticsDataDto {
    private TotalStatisticsDto total;
    private List<DetailedStatisticsItemDto> detailed;
}
