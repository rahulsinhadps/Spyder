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
    private Set<String> categoryPages;
    private Set<String> productFamilyPages;
    private Set<String> productDetailsPages;
    private Set<String> counterBookPages;
    private Set<String> productListPages;
    private static int resultSize;
    private long startTime;
    private static String outputFileName;

    public BasicWebCrawler() {
        links = new HashSet<>();
        categoriesVisited = new HashSet<>();
        categoryListPages = new HashSet<>();
        productDetailsPages = new HashSet<>();
        productFamilyPages = new HashSet<>();
        counterBookPages = new HashSet<>();
        productListPages = new HashSet<>();
        categoryPages = new HashSet<>();
        startTime = System.currentTimeMillis();
    }


    public static void main(String[] args) {
        if(args.length == 3) {
            final String url = args[0];
            outputFileName = args[1] + ".txt";
            resultSize = Integer.parseInt(args[2]);
            disableCertificateCheck();
            new BasicWebCrawler().getPageLinks(url);
        }
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
                    if (page.attr("abs:href").contains("fastenal.ca")
                            && !page.attr("abs:href").contains("footer")) {
                        String[] urlArray = page.attr("abs:href").split("/");
                        final String categoryIdWithReqParams = urlArray[urlArray.length - 1];
                        final int reqParamsIndex = categoryIdWithReqParams.indexOf('?');
                        String categoryId = reqParamsIndex != -1
                                ? categoryIdWithReqParams.substring(0, categoryIdWithReqParams.indexOf('?'))
                                : categoryIdWithReqParams;
                        categoryId = categoryIdWithReqParams;
                        categoryId = categoryId.contains("#")
                                ? categoryId.substring(0, categoryId.length() - 1)
                                : categoryId;
                        if (!categoriesVisited.contains(categoryId)) {
                            categoriesVisited.add(categoryId);
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
        } else if (url.contains("category") && (categoryPages.size() < resultSize)) {
            categoryPages.add(url);
        }
    }


    private boolean areAllCategoriesFound() {
        final boolean productPagesFound = productDetailsPages.size() >= resultSize
                && counterBookPages.size() >= resultSize
                && productListPages.size() >= resultSize;
        final boolean categoryPagesFound = categoryListPages.size() >= resultSize
                && productFamilyPages.size() >= resultSize
                && categoryPages.size() >= resultSize;
        return (productPagesFound && categoryPagesFound);
    }

    private void printPageLinks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName, true))) {
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
            bw.write("----------------------");
            bw.newLine();
            bw.write("Category Pages");
            bw.newLine();
            categoryPages.forEach(page -> {
                try {
                    bw.write(page);
                    bw.newLine();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            });
            final String totalTimeTaken = (System.currentTimeMillis() - startTime)/1000 + " seconds";
            System.out.println(totalTimeTaken);
            bw.write("----------------");
            bw.newLine();
            bw.write("Total time taken: " + totalTimeTaken);
            bw.newLine();


        } catch (final IOException e) {

            e.printStackTrace();

        }
        System.exit(0);
    }


    private static void disableCertificateCheck() {
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

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
        }
    }
}