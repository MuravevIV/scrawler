package com.ilyamur.scrawler;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.dom.Document;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

class Crawler {

    public static class Builder {

        private ExecutorService executionService;

        public Builder setExecutionService(ExecutorService executionService) {
            this.executionService = executionService;
            return this;
        }

        public Crawler build() {
            checkNotNull(executionService, "Execution service should be defined");
            return new Crawler(this);
        }
    }

    final ExecutorService executionService;
    private final ConcurrentMap<Long, BrowserEngine> browserEngines = new ConcurrentHashMap<>();

    public Crawler(Builder builder) {
        executionService = builder.executionService;
    }

    public void get(String uri, Consumer<DocumentContext> consumer) {
        executionService.execute(() -> {
            Document document = getBrowserEngine(Thread.currentThread().getId()).navigate(uri).getDocument();
            DocumentContext documentContext = new DocumentContext(document);
            consumer.accept(documentContext);
        });
    }

    private BrowserEngine getBrowserEngine(long threadId) {
        BrowserEngine browserEngine = browserEngines.get(threadId);
        if (browserEngine == null) {
            browserEngine = getWebKit();
            browserEngines.put(threadId, browserEngine);
        }
        return browserEngine;
    }

    @VisibleForTesting
    BrowserEngine getWebKit() {
        return BrowserFactory.getWebKit();
    }

    public void get(String uri, Runnable r) {
        get(uri, documentContext -> r.run());
    }

    public void shutdown() {
        executionService.shutdown();
        browserEngines.values().forEach(BrowserEngine::shutdown);
    }

    public static Crawler getDefault() {
        return (new Builder())
                .setExecutionService(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                .build();
    }
}
