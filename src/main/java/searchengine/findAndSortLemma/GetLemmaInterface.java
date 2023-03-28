package searchengine.findAndSortLemma;

import java.util.HashMap;
import java.util.List;

public interface GetLemmaInterface {
    HashMap<String, Integer> getLemmaList(String content);
    List<String> getLemma(String word);
    List<Integer> findIndexLemmaInText(String content, String lemma);
}
