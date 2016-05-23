package com.ilyamur.scrawler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JApplication {

    public static void main(String[] args) {
        (new JApplication()).run();
    }

    private void run() {
        Crawler crawler = Crawler.getDefault();
        crawler.get("http://www.deviantart.com", pageCtx -> {
            pageCtx.select(".grid-dailydev .tt-a.thumb", divCtx -> {
                Matcher m = Pattern.compile(".+/(.+.jpg)").matcher(divCtx.attr("data-super-full-img"));
                if (m.matches()) {
                    String src = m.group(0);
                    String name = m.group(1);
                    crawler.get(src, imageCtx -> {
                        imageCtx.writeToFile("D:/dailydev/" + name);
                    });
                }
            });
        });
    }
}
