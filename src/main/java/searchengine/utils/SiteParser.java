package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

@Slf4j
public class SiteParser extends RecursiveAction {


    private String url;

    private SiteManager manager;

     private volatile Connection.Response connection;

     private volatile Document document;


    public SiteParser(String url, SiteManager manager) {
        this.url = url;
        this.manager = manager;
    }

    public SiteParser(){}



    @Override
    protected void compute() {
        String CSS_QUERY = "a[href]";
        String ATTRIBUTE_KEY = "href";

        try {
            Page page = buildPage(url);
            manager.write2db(page);

            if (page.getCode() < 400){

                Elements elements = document.select(CSS_QUERY);
                List<SiteParser> siteParsers = new ArrayList<>();

                for (Element element : elements) {
                    String attributeUrl = element.absUrl(ATTRIBUTE_KEY);
                    if (manager.isValid(attributeUrl)){
                        manager.add2List(attributeUrl);
                        SiteParser parser = new SiteParser(attributeUrl, manager);
                        siteParsers.add(parser);
                    }
                }
                invokeAll(siteParsers);
            }
        } catch (IOException exception) {
            manager.errorReport(url, exception);
        }
    }


    public synchronized Page buildPage(String url) throws IOException {
        initConnectionAndDocument(url);
        int statusCode = connection.statusCode();

        if (statusCode < 400){
            return Page.pageBuilder()
                    .path(connection.url().getPath())
                    .content(document.html())
                    .code(connection.statusCode())
                    .build();
        } else return Page.pageBuilder()
                .path(connection.url().getPath())
                .code(connection.statusCode())
                .build();
    }


    private void initConnectionAndDocument(String url) throws IOException {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .execute();
        document = connection.parse();
    }



}
