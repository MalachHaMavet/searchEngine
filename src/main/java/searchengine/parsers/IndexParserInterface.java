package searchengine.parsers;

import searchengine.dto.statistics.StatisticsIndexDto;
import searchengine.model.SitePage;

import java.util.List;

public interface IndexParserInterface {
    void run(SitePage site);
    List<StatisticsIndexDto> getIndexList();
}
