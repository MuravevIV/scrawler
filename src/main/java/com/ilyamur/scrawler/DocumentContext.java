package com.ilyamur.scrawler;

import com.google.common.annotations.VisibleForTesting;
import com.ui4j.api.dom.Document;
import com.ui4j.api.dom.Element;

import java.util.function.Consumer;

class DocumentContext extends ResourceContext {

    private Document document;

    public DocumentContext(Document document) {
        this.document = document;
    }

    public void select(String selector, Consumer<ElementContext> consumer) {
        document.queryAll(selector).forEach(element -> {
            consumer.accept(createElementContext(element));
        });
    }

    @VisibleForTesting
    ElementContext createElementContext(Element element) {
        return new ElementContext(element);
    }

    public void select(String s, Runnable r) {
        select(s, elementContext -> {
            r.run();
        });
    }
}
