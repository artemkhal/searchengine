package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Page;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
public class Morphology {

    public HashMap<String, Integer> analyse(String text) throws IOException {
        val hashMap = new HashMap<String, Integer>();
        val russianLuceneMorphology = new RussianLuceneMorphology();
        val collect = Arrays.stream(text.split(" "))
                .map(this::getIgnoredMarks)
                .filter(item -> isValid(item, russianLuceneMorphology))
                .map(item -> getNormalForm(item, russianLuceneMorphology))
                .toList();

        for (List<String> normalWord : collect) {
            add2map(normalWord, hashMap);
        }
        return hashMap;
    }

    private void add2map(List<String> normalWord, HashMap<String, Integer> hashMap) {
        for (String word : normalWord) {
            if (!hashMap.containsKey(word)){
                hashMap.put(word, 1);
            }else {
                int res = hashMap.get(word) + 1;
                hashMap.put(word, res);
            }
        }
    }


    public String getText2Html(List<Page> pages) {
        return pages.stream().map(this::getText2Html).collect(Collectors.joining());
    }


    public String getText2Html(Page page) {
        if (page.getCode() < 400){
            Document document = Jsoup.parse(page.getContent());
            return document.title() + document.body().text();
        }

        return null;
    }

    private List<String> getNormalForm(String word, RussianLuceneMorphology russianLuceneMorphology) {
        return russianLuceneMorphology.getNormalForms(word);
    }



    private boolean isValid(String word, LuceneMorphology analyser) {
        val parts = List.of("ЧАСТ", "СОЮЗ", "МЕЖД", "ПРЕДЛ");
        val morphInfo = analyser.getMorphInfo(word);
        for (String part : parts) {
            for (String info : morphInfo) {
                if (info.contains(part)) return false;
            }
        }
        return true;
    }

    private String getIgnoredMarks(String word) {
        String[] marks = {"`", "~", "!", "@", "#", "$", "%", "^", "&",
                "*", "(", ")", "_", "-", "+", "=", "{", "[", "]",
                "}", "\\", "|", "\"", "'",
                ":", ";", ",", "<", ">", ".", "?", "/"};
        for (String mark : marks) {
            word = word.replace(mark, "");
        }

        return word.trim().toLowerCase(Locale.ROOT);
    }


}
