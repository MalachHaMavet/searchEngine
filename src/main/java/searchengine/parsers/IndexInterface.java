package searchengine.parsers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.dto.statistics.StatisticsIndexDto;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SitePage;
import searchengine.findAndSortLemma.GetLemmaInterface;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexInterface implements IndexParserInterface {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final GetLemmaInterface getLemmaInterface;
    private List<StatisticsIndexDto> statisticsIndexDtoList;

    @Override
    public List<StatisticsIndexDto> getIndexList() {
        return statisticsIndexDtoList;
    }

    @Override
    public void run(SitePage site) {
        Iterable<Page> pageList = pageRepository.findBySiteId(site);
        List<Lemma> lemmaList = lemmaRepository.findBySitePageId(site);
        statisticsIndexDtoList = new ArrayList<>();

        for (Page page : pageList) {
            if (page.getCode() < 400) {
                long pageId = page.getId();
                String pageContent = page.getContent();
                String title = ClearHTML.clear(pageContent, "title");
                String body = ClearHTML.clear(pageContent, "body");
                HashMap<String, Integer> titleList = getLemmaInterface.getLemmaList(title);
                HashMap<String, Integer> bodyList = getLemmaInterface.getLemmaList(body);

                for (Lemma lemma : lemmaList) {
                    Long lemmaId = lemma.getId();
                    String theExactLemma = lemma.getLemma();
                    if (titleList.containsKey(theExactLemma) || bodyList.containsKey(theExactLemma)) {
                        float rank = 0.0F;
                        if (titleList.get(theExactLemma) != null) {
                            Float titleRank = Float.valueOf(titleList.get(theExactLemma));
                            rank += titleRank;
                        }
                        if (bodyList.get(theExactLemma) != null) {
                            float bodyRank = (float) (bodyList.get(theExactLemma) * 0.8);
                            rank += bodyRank;
                        }
                        statisticsIndexDtoList.add(new StatisticsIndexDto(pageId, lemmaId, rank));
                    } else {
                        log.debug("Dont found the lemma");
                    }
                }
            } else {
                log.debug("Status code is bad - " + page.getCode());
            }
        }
    }


}
