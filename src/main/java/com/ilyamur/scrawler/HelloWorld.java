package com.ilyamur.scrawler;

import static com.ui4j.api.browser.BrowserFactory.getWebKit;

import com.ui4j.api.browser.Page;

public class HelloWorld {

    public static void main(String[] args) {

        try (Page page = getWebKit().navigate("https://news.ycombinator.com")) {
            page
                    .getDocument()
                    .queryAll(".title a")
                    .forEach(e -> {
                        System.out.println(e.getText().get());
                    });
        }
    }
}
