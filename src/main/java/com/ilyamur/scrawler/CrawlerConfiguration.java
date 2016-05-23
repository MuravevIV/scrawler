package com.ilyamur.scrawler;

import java.util.concurrent.ExecutorService;

/**
 * @author Ilya_Muravyev
 */
public class CrawlerConfiguration {

    private ExecutorService executionService;

    public void setExecutionService(ExecutorService executionService) {
        this.executionService = executionService;
    }

    public ExecutorService getExecutionService() {
        return executionService;
    }
}
