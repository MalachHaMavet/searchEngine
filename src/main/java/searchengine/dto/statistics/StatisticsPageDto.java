package searchengine.dto.statistics;

import lombok.Value;

@Value
public class StatisticsPageDto {
    String url;
    String content;
    int code;
}
