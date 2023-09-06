package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Page;

import java.io.IOException;
import java.util.*;

@Slf4j
public class Morphology {

    private static LuceneMorphology russianLuceneMorphology;
    private static LuceneMorphology englishLuceneMorphology;

    static {
        try {
            russianLuceneMorphology = new RussianLuceneMorphology();
            englishLuceneMorphology = new EnglishLuceneMorphology();
        } catch (IOException e) {
            log.info("Не удалось инициализировать морфологический анализатор");
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, Integer> analyse(Page page){
        String text = getTextFromHtml(page);
        if (text != null)
            return analyse(text);
        else return new HashMap<>();
    }

    public static HashMap<String, Integer> analyse(String text){
        val hashMap = new HashMap<String, Integer>();
        val collect = Arrays.stream(text.split(" "))
                .map(Morphology::getIgnoredMarks)
                .filter(Morphology::isValid)
                .map(Morphology::getNormalForm)
                .toList();

        for (List<String> normalWord : collect) {
            add2map(normalWord, hashMap);
        }
        return hashMap;
    }

    private static void add2map(List<String> normalWord, HashMap<String, Integer> hashMap) {

        for (String word : normalWord) {
            if (word != null) {
                if (!hashMap.containsKey(word)) {
                    hashMap.put(word, 1);
                } else {
                    int res = hashMap.get(word) + 1;
                    hashMap.put(word, res);
                }
            }
        }
    }

    private static String getTextFromHtml(Page page) {
        if (page.getCode() < 400) {
            Document document = Jsoup.parse(page.getContent());
            return document.title() + document.body().text();
        }
        return null;
    }

    public static String getTitle(Page page){
        val parse = Jsoup.parse(page.getContent());
        return parse.title();
    }

    private static List<String> getNormalForm(String word) {
        try {
            return russianLuceneMorphology.getNormalForms(word);
        } catch (WrongCharaterException e) {
//            log.info(e.getLocalizedMessage());
            try {
                return englishLuceneMorphology.getNormalForms(word);
            } catch (WrongCharaterException ex) {
//                log.info(ex.getLocalizedMessage());
            }
        }
        return new ArrayList<>();
    }


    private static boolean isValid(String word) {
        val parts = List.of("ЧАСТ", "СОЮЗ", "МЕЖД", "ПРЕДЛ");

        if (word.equals("") || word.equals(" ")){
            return false;
        }
        try {
            val morphInfo = russianLuceneMorphology.getMorphInfo(word);
            for (String part : parts) {
                for (String info : morphInfo) {
                    if (info.contains(part)) return false;
                }
            }
        } catch (WrongCharaterException e) {
//            log.info(e.getLocalizedMessage());
            try {
                val morphInfo = englishLuceneMorphology.getMorphInfo(word);
                for (String part : parts) {
                    for (String info : morphInfo) {
                        if (info.contains(part)) return false;
                    }
                }
            } catch (WrongCharaterException ex) {
//                log.info(ex.getLocalizedMessage());
            }
        }
        return true;
    }

    private static String getIgnoredMarks(String word) {
        String[] marks = {"`", "~", "!", "@", "#", "$", "%", "^", "&",
                "*", "(", ")", "_", "-", "+", "=", "{", "[", "]",
                "}", "\\", "|", "\"", "'",
                ":", ";", ",", "<", ">", ".", "?", "/"};
        for (String mark : marks) {
            word = word.replace(mark, "");
        }

        return word.trim().toLowerCase(Locale.ROOT);
    }


    public static String getSnippet(ArrayList<Map.Entry<String, Integer>> query, Page page) {
        StringBuilder builder = new StringBuilder();
        val split = getTextFromHtml(page).split("\\p{Punct}|\\s");
        List<Integer> wordIndexes = getWordIndexes(split, query);
        builder.append(". . .");
        for (Integer index : wordIndexes) {
            builder.append("<b>" + split[index] + "</b> ");
            for (int i = index + 1; i < index + 7 && i < split.length; i++) {
                builder.append(split[i] + " ");
            }
            builder.append(". . .");
            builder.append("\n");
        }

        return builder.toString();
    }

    private static List<Integer> getWordIndexes(String[] split, ArrayList<Map.Entry<String, Integer>> query) {
        List<Integer> wordIndexes = new ArrayList<>();
        val iterator = Arrays.stream(split).iterator();
        int wordIndex = 0;
        while(iterator.hasNext()){
            val word = iterator.next().toLowerCase(Locale.ROOT);
            if (!word.isEmpty()){
                val normalForm = getNormalForm(word);
                for (String s : normalForm) {
                    for (Map.Entry<String, Integer> entry : query) {
                        if (entry.getKey().equals(s)){
                            wordIndexes.add(wordIndex);
                        }
                    }
                }
            }
            wordIndex++;
        }
        return wordIndexes;
    }

}
