package com.ajax;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

public class BasicWebCrawler {

    private HashSet<String> links;
    private HashSet<String> categoriesVisited;
    private static final int MAX_DEPTH = 3;

    public BasicWebCrawler() {
        links = new HashSet<String>();
        categoriesVisited = new HashSet<String>();
    }

    public void getPageLinks(final String URL,
                             int depth) {
        //4. Check if you have already crawled the URLs
        //(we are intentionally not checking for duplicate content in this example)
        if (!links.contains(URL) && depth < MAX_DEPTH) {
            try {
                //4. (i) If not add it to the index
                if (links.add(URL)) {
                    System.out.println(URL);
                }

                //2. Fetch the HTML code
                Document document = Jsoup.connect(URL).get();
                //3. Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                //5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    if (page.attr("abs:href").contains("fastenal")
                            && page.attr("abs:href").contains("category")) {
                        String[] urlArray = page.attr("abs:href").split("/");
                        if(!categoriesVisited.contains(urlArray[urlArray.length - 1])) {
                            categoriesVisited.add(urlArray[urlArray.length - 1]);
                            getPageLinks(page.attr("abs:href"), depth);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        //1. Pick a URL from the frontier
        new BasicWebCrawler().getPageLinks("https://www.example.com/", 0);
    }

}