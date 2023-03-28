package searchengine.services;

import searchengine.dto.statistics.StatisticsSearchDto;

import java.util.List;

public interface SearchService {
    List<StatisticsSearchDto> allSiteSearch(String text, int offset, int limit);
    List<StatisticsSearchDto> siteSearch(String searchText, String url, int offset, int limit);
}
