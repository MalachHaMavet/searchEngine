package searchengine.parsers;

import searchengine.dto.statistics.StatisticsLemmaDto;
import searchengine.model.SitePage;

import java.util.List;

public interface LemmaParserInterface {
    void run(SitePage site);
    List<StatisticsLemmaDto> getLemmaDtoList();
}
