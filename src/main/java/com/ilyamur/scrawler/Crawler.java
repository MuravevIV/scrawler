package com.ilyamur.scrawler;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

class Crawler {

    private ExecutorService executionService;

    public void setConfiguration(CrawlerConfiguration configuration) {
        executionService = configuration.getExecutionService();
    }

    public void get(String uri, Consumer<DocumentContext> r) {
        checkNotNull(executionService, "Configuration error");
        executionService.execute(() -> r.accept(new DocumentContext()));
    }

    public void get(String uri, Runnable r) {
        get(uri, documentContext -> {
            r.run();
        });
    }
}
