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


    private final String url;

    private final SiteManager manager;


    public SiteParser(String url, SiteManager manager) {
        this.url = url;
        this.manager = manager;
    }


    @Override
    protected void compute() {
        String CSS_QUERY = "a[href]";
        String ATTRIBUTE_KEY = "href";

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            Connection.Response connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .execute();

            Document document = connection.parse();
            int statusCode = connection.statusCode();

            if (statusCode < 400) {
                Page page = Page.pageBuilder()
                        .path(connection.url().getPath())
                        .content(document.html())
                        .code(connection.statusCode())
                        .build();

                manager.write2db(page);

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
            } else {
                Page page = Page.pageBuilder()
                        .path(connection.url().getPath())
                        .code(connection.statusCode())
                        .build();
                manager.write2db(page);
            }

        } catch (IOException exception) {
            manager.errorReport(url, exception);
        }
    }



}
