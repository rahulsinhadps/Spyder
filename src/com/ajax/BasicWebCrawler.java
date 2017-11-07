package com.ajax;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;


public class BasicWebCrawler {

    private HashSet<String> links;
    private HashSet<String> categoriesVisited;
    private Set<String> categoryListPages;
    private Set<String> productFamilyPages;
    private Set<String> productDetailsPages;
    private Set<String> counterBookPages;
    private Set<String> productListPages;
    private int resultSize;
    private long startTime;

    public BasicWebCrawler() {
        links = new HashSet<>();
        categoriesVisited = new HashSet<>();
        categoryListPages = new HashSet<>();
        productDetailsPages = new HashSet<>();
        productFamilyPages = new HashSet<>();
        counterBookPages = new HashSet<>();
        productListPages = new HashSet<>();
        resultSize = 5;
        startTime = System.currentTimeMillis();
    }


    public static void main(String[] args) {
        final String url = "https://www.fastenal.ca/";
        disableCertificateCheck(url);
        new BasicWebCrawler().getPageLinks(url);
    }

    public void getPageLinks(final String url) {
        if (!links.contains(url)) {
            try {
                setCategoryBooleanForResultType(url);
                links.add(url);

                System.out.println(url);


                if (areAllCategoriesFound()) {
                    printPageLinks();
                }

                Document document = Jsoup.connect(url).get();
                Elements linksOnPage = document.select("a[href]");

                for (final Element page : linksOnPage) {
                    if (page.attr("abs:href").contains("fastenal.ca")) {
                        String[] urlArray = page.attr("abs:href").split("/");
                        if (!categoriesVisited.contains(urlArray[urlArray.length - 1])) {
                            categoriesVisited.add(urlArray[urlArray.length - 1]);
                            getPageLinks(page.attr("abs:href"));
                        }
                    }
                }
            } catch (final IOException e) {
                System.err.println("For '" + url + "': " + e.getMessage());
            }
        }
    }

    private void setCategoryBooleanForResultType(final String url) {
        if (url.contains("productFamily") && (productFamilyPages.size() < resultSize)) {
            productFamilyPages.add(url);
        } else if (url.contains("categoryList") && (categoryListPages.size() < resultSize)) {
            categoryListPages.add(url);
        } else if (url.contains("counterBook") && (counterBookPages.size() < resultSize)) {
            counterBookPages.add(url);
        } else if ((url.contains("sku") || url.contains("productDetail")) && (productDetailsPages.size() < resultSize)) {
            productDetailsPages.add(url);
        } else if (url.contains("productList") && productListPages.size() < resultSize) {
            productListPages.add(url);
        }
    }

    private void printPageLinks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("URLs.txt", true))) {
            bw.write("Product Details Pages");
            bw.newLine();
            productDetailsPages.forEach(page -> {
                try {
                    bw.write(page);
                    bw.newLine();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            });
            bw.write("--------------------------");
            bw.newLine();
            bw.write("Product Family Pages");
            bw.newLine();
            productFamilyPages.forEach(page -> {
                try {
                    bw.write(page);
                    bw.newLine();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            });
            bw.write("-------------------------");
            bw.newLine();
            bw.write("Category List Pages");
            bw.newLine();
            categoryListPages.forEach(page -> {
                try {
                    bw.write(page);
                    bw.newLine();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            });
            bw.write("----------------------");
            bw.newLine();
            bw.write("Counterbook Pages");
            bw.newLine();
            counterBookPages.forEach(page -> {
                try {
                    bw.write(page);
                    bw.newLine();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            });
            bw.write("----------------------");
            bw.newLine();
            bw.write("Product List Pages");
            bw.newLine();
            productListPages.forEach(page -> {
                try {
                    bw.write(page);
                    bw.newLine();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            });
            bw.write("----------------");
            bw.newLine();
            bw.write("Total time taken: " + (System.currentTimeMillis() - startTime)/1000 + "seconds");
            bw.newLine();


        } catch (final IOException e) {

            e.printStackTrace();

        }

        System.exit(0);
    }

    private static void disableCertificateCheck(final String urlString) {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            X509Certificate[] certs,
                            String authType) {
                    }

                    public void checkServerTrusted(
                            X509Certificate[] certs,
                            String authType) {
                    }
                }
        };

// Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
        }
    }

    private boolean areAllCategoriesFound() {
        final boolean productPagesFound = productDetailsPages.size() >= resultSize
                && counterBookPages.size() >= resultSize
                && productListPages.size() >= resultSize;
        final boolean categoryPagesFound = categoryListPages.size() >= resultSize && productFamilyPages.size() >= resultSize;
        return (productPagesFound && categoryPagesFound);
    }
}