package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsSearchDto;
import searchengine.dto.statistics.StatisticsResponseDto;
import searchengine.dto.statistics.BadRequestDto;
import searchengine.dto.statistics.SearchResultsDto;
import searchengine.dto.statistics.ResponseDto;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SiteRepository siteRepository;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SiteRepository siteRepository, SearchService searchService) {

        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.siteRepository = siteRepository;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponseDto> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Object> startIndexing() {
        if (indexingService.indexingAllUrl()) {
            return new ResponseEntity<>(new ResponseDto(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadRequestDto(false, "Indexing not started"),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing() {
        if (indexingService.stopIndexing()) {
            return new ResponseEntity<>(new ResponseDto(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadRequestDto(false,
                    "The index is not stopped because it is not started"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "query", required = false, defaultValue = "")
                                         String request, @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                         @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (request.isEmpty()) {
            return new ResponseEntity<>(new BadRequestDto(false, "Request is not liquid"), HttpStatus.BAD_REQUEST);
        } else {
            List<StatisticsSearchDto> searchData;
            if (!site.isEmpty()) {
                if (siteRepository.findByUrl(site) == null) {
                    return new ResponseEntity<>(new BadRequestDto(false, "The requested page was not found"),
                            HttpStatus.BAD_REQUEST);
                } else {
                    searchData = searchService.siteSearch(request, site, offset, limit);
                }
            } else {
                searchData = searchService.allSiteSearch(request, offset, limit);
            }
            return new ResponseEntity<>(new SearchResultsDto(true, searchData.size(), searchData), HttpStatus.OK);
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Object> indexPage(@RequestParam(name = "url") String url) {
        if (url.isEmpty()) {
            log.info("This page is not defined");
            return new ResponseEntity<>(new BadRequestDto(false, "This page is not defined"), HttpStatus.BAD_REQUEST);
        } else {
            if (indexingService.urlIndexing(url) == true) {
                log.info("Page - " + url + " - added to the reindexing queue");
                return new ResponseEntity<>(new ResponseDto(true), HttpStatus.OK);
            } else {
                log.info("Required page from the configuration file");
                return new ResponseEntity<>(new BadRequestDto(false, "Required page from the configuration file"),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }
}

