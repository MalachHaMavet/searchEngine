package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItemDto;
import searchengine.dto.statistics.StatisticsDataDto;
import searchengine.dto.statistics.StatisticsResponseDto;
import searchengine.dto.statistics.TotalStatisticsDto;

import searchengine.model.SitePage;
import searchengine.model.Status;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticsService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;

    private TotalStatisticsDto getTotal() {
        Long sites = siteRepository.count();
        Long pages = pageRepository.count();
        Long lemmas = lemmaRepository.count();
        return new TotalStatisticsDto(sites, pages, lemmas, true);
    }

    private DetailedStatisticsItemDto getDetailed(SitePage site) {
        String url = site.getUrl();
        String name = site.getName();
        Status status = site.getStatus();
        Date statusTime = site.getStatusTime();
        String error = site.getLastError();
        long pages = pageRepository.countBySiteId(site);
        long lemmas = lemmaRepository.countBySitePageId(site);
        return new DetailedStatisticsItemDto(url, name, status, statusTime, error, pages, lemmas);
    }

    private List<DetailedStatisticsItemDto> getDetailedList() {
        List<SitePage> siteList = siteRepository.findAll();
        List<DetailedStatisticsItemDto> result = new ArrayList<>();
        for (SitePage site : siteList) {
            DetailedStatisticsItemDto item = getDetailed(site);
            result.add(item);
        }
        return result;
    }


    @Override
    public StatisticsResponseDto getStatistics() {
        TotalStatisticsDto total = getTotal();
        List<DetailedStatisticsItemDto> list = getDetailedList();
        return new StatisticsResponseDto(true, new StatisticsDataDto(total, list));
    }
}
