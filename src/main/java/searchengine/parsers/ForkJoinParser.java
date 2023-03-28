package searchengine.parsers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.statistics.StatisticsPageDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;


@Slf4j
public class ForkJoinParser extends RecursiveTask<List<StatisticsPageDto>> {
    private final String url;
    private final List<String> urlList;
    private final List<StatisticsPageDto> statisticsPageDtoList;

    public ForkJoinParser(String url, List<StatisticsPageDto> statisticsPageDtoList, List<String> urlList) {
        this.url = url;
        this.statisticsPageDtoList = statisticsPageDtoList;
        this.urlList = urlList;
    }

    public Document getConnect(String url) {
        Document document = null;
        try {
            Thread.sleep(150);
            document = Jsoup.connect(url).userAgent(UserAgent.getUserAgent()).referrer("http://www.google.com").get();
        } catch (Exception e) {
            log.debug("Can't get connected to the site" + url);
        }
        return document;
    }

    @Override
    protected List<StatisticsPageDto> compute() {
        try {
            Thread.sleep(150);
            Document document = getConnect(url);
            String html = document.outerHtml();
            Connection.Response response = document.connection().response();
            int statusCode = response.statusCode();
            StatisticsPageDto statisticsPageDto = new StatisticsPageDto(url, html, statusCode);
            statisticsPageDtoList.add(statisticsPageDto);
            Elements elements = document.select("body").select("a");
            List<ForkJoinParser> taskList = new ArrayList<>();
            for (Element el : elements) {
                String link = el.attr("abs:href");
                if (link.startsWith(el.baseUri())
                        && !link.equals(el.baseUri())
                        && !link.contains(".png") && !urlList.contains(link)
                        && !link.contains(".jpg") && !link.contains(".JPG")
                        && !link.contains("#") && !link.contains(".pdf") ) {
                    urlList.add(link);
                    ForkJoinParser task = new ForkJoinParser(link, statisticsPageDtoList, urlList);
                    task.fork();
                    taskList.add(task);
                }
            }
            taskList.forEach(ForkJoinTask::join);
        } catch (Exception e) {
            log.debug("Error in parsing this address : " + url);
            StatisticsPageDto statisticsPageDto = new StatisticsPageDto(url, "", 500);
            statisticsPageDtoList.add(statisticsPageDto);
        }
        return statisticsPageDtoList;
    }

}