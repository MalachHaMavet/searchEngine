package searchengine.parsers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsIndexDto;
import searchengine.dto.statistics.StatisticsLemmaDto;
import searchengine.dto.statistics.StatisticsPageDto;
import searchengine.model.*;
import searchengine.repository.IndexSearchRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;


import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
@Slf4j
public class IndexingSite implements Runnable {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private static final int amountPoolInProcessor = Runtime.getRuntime().availableProcessors();
    private final IndexSearchRepository indexSearchRepository;
    private final LemmaParserInterface lemmaParserInterface;
    private final IndexParserInterface indexParserInterface;
    private final String url;
    private final SitesList sitesList;

    private List<StatisticsPageDto> getPageEntityDtoList() throws InterruptedException {
        if (!Thread.interrupted()) {
            String urlFormat = url + "/";
            List<StatisticsPageDto> statisticsPageDtoVector = new Vector<>();
            List<String> urlList = new Vector<>();
            ForkJoinPool forkJoinPool = new ForkJoinPool(amountPoolInProcessor);
            List<StatisticsPageDto> pages = forkJoinPool.invoke(new ForkJoinParser(urlFormat, statisticsPageDtoVector, urlList));
            return new CopyOnWriteArrayList<>(pages);
        } else throw new InterruptedException();
    }

    @Override
    public void run() {
        if (siteRepository.findByUrl(url) != null) {
            log.info("Start delete site data - " + url);
            deleteDataFromSite();
        }
        log.info("Indexing this - " + url + " " + getName());
        saveSiteInDataBase();
        try {
            List<StatisticsPageDto> statisticsPageDtoList = getPageEntityDtoList();
            saveInBase(statisticsPageDtoList);
            getLemmasPage();
            indexingWords();
        } catch (InterruptedException e) {
            log.error("Indexing was stopped - " + url);
            errorIndexingSite();
        }
    }

    private void getLemmasPage() {
        if (!Thread.interrupted()) {
            SitePage sitePage = siteRepository.findByUrl(url);
            sitePage.setStatusTime(new Date());
            lemmaParserInterface.run(sitePage);
            List<StatisticsLemmaDto> statisticsLemmaDtoList = lemmaParserInterface.getLemmaDtoList();
            List<Lemma> lemmaList = new CopyOnWriteArrayList<>();
            for (StatisticsLemmaDto statisticsLemmaDto : statisticsLemmaDtoList) {
                lemmaList.add(new Lemma(statisticsLemmaDto.getLemma(), statisticsLemmaDto.getFrequency(), sitePage));
            }
            lemmaRepository.flush();
            lemmaRepository.saveAll(lemmaList);
        } else {
            throw new RuntimeException();
        }
    }

    private void saveInBase(List<StatisticsPageDto> pages) throws InterruptedException {
        if (!Thread.interrupted()) {
            List<Page> pageList = new CopyOnWriteArrayList<>();
            SitePage site = siteRepository.findByUrl(url);
            for (StatisticsPageDto page : pages) {
                int first = page.getUrl().indexOf(url) + url.length();
                String format = page.getUrl().substring(first);
                pageList.add(new Page(site, format, page.getCode(),
                        page.getContent()));
            }
            pageRepository.flush();
            pageRepository.saveAll(pageList);
        } else {
            throw new InterruptedException();
        }
    }

    private void deleteDataFromSite() {
        SitePage sitePage = siteRepository.findByUrl(url);
        sitePage.setStatus(Status.INDEXING);
        sitePage.setName(getName());
        sitePage.setStatusTime(new Date());
        siteRepository.save(sitePage);
        siteRepository.flush();
        siteRepository.delete(sitePage);
    }

    private void indexingWords() throws InterruptedException {
        if (!Thread.interrupted()) {
            SitePage sitePage = siteRepository.findByUrl(url);
            indexParserInterface.run(sitePage);
            List<StatisticsIndexDto> statisticsIndexDtoList = new CopyOnWriteArrayList<>(indexParserInterface.getIndexList());
            List<IndexSearch> indexList = new CopyOnWriteArrayList<>();
            sitePage.setStatusTime(new Date());
            for (StatisticsIndexDto statisticsIndexDto : statisticsIndexDtoList) {
                Page page = pageRepository.getById(statisticsIndexDto.getPageID());
                Lemma lemma = lemmaRepository.getById(statisticsIndexDto.getLemmaID());
                indexList.add(new IndexSearch(page, lemma, statisticsIndexDto.getRank()));
            }
            indexSearchRepository.flush();
            indexSearchRepository.saveAll(indexList);
            log.info("Indexing is Done - " + url);
            sitePage.setStatusTime(new Date());
            sitePage.setStatus(Status.INDEXED);
            siteRepository.save(sitePage);
        } else {
            throw new InterruptedException();
        }
    }

    private void saveSiteInDataBase() {
        SitePage sitePage = new SitePage();
        sitePage.setUrl(url);
        sitePage.setName(getName());
        sitePage.setStatus(Status.INDEXING);
        sitePage.setStatusTime(new Date());
        siteRepository.flush();
        siteRepository.save(sitePage);
    }

    private String getName() {
        List<Site> sitesList_2 = sitesList.getSites();
        for (Site map : sitesList_2) {
            if (map.getUrl().equals(url)) {
                return map.getName();
            }
        }
        return "";
    }

    private void errorIndexingSite() {
        SitePage sitePage = new SitePage();
        sitePage.setLastError("Stop indexing");
        sitePage.setStatus(Status.FAILED);
        sitePage.setStatusTime(new Date());
        siteRepository.save(sitePage);
    }
}

