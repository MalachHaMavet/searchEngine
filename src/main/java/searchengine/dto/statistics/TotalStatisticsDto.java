package searchengine.dto.statistics;

import lombok.Value;

@Value
public class TotalStatisticsDto {
     Long sites;
     Long pages;
     Long lemmas;
     boolean indexing;
}
