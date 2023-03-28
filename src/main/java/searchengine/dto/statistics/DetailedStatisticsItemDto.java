package searchengine.dto.statistics;

import lombok.Value;
import searchengine.model.StatusEnum;

import java.util.Date;

@Value
public class DetailedStatisticsItemDto {
    String url;
    String name;
    StatusEnum statusEnum;
    Date statusTime;
    String error;
    long pages;
    long lemmas;
}
