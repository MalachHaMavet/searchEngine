package searchengine.parsers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.dto.statistics.StatisticsLemmaDto;
import searchengine.model.Page;
import searchengine.model.SitePage;
import searchengine.findAndSortLemma.GetLemmaInterface;
import searchengine.repository.PageRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class LemmaCreationInterface implements LemmaParserInterface {
    private final PageRepository pageRepository;
    private final GetLemmaInterface getLemmaInterface;
    private List<StatisticsLemmaDto> statisticsLemmaDtoList;

    public List<StatisticsLemmaDto> getLemmaDtoList() {
        return statisticsLemmaDtoList;
    }

    @Override
    public void run(SitePage site) {
        statisticsLemmaDtoList = new CopyOnWriteArrayList<>();
        Iterable<Page> pageList = pageRepository.findAll();
        TreeMap<String, Integer> lemmasList = new TreeMap<>();
        for (Page page : pageList) {
            String pageContent = page.getContent();
            String title = ClearHTML.clear(pageContent, "title");
            String body = ClearHTML.clear(pageContent, "body");
            HashMap<String, Integer> titleList = getLemmaInterface.getLemmaList(title);
            HashMap<String, Integer> bodyList = getLemmaInterface.getLemmaList(body);
            Set<String> allTheWords = new HashSet<>();
            allTheWords.addAll(titleList.keySet());
            allTheWords.addAll(bodyList.keySet());
            for (String word : allTheWords) {
                int frequency = lemmasList.getOrDefault(word, 0) + 1;
                lemmasList.put(word, frequency);
            }
        }
        for (String lemma : lemmasList.keySet()) {
            Integer frequency = lemmasList.get(lemma);
            statisticsLemmaDtoList.add(new StatisticsLemmaDto(lemma, frequency));
        }
    }


}
