package com.ilyamur.scrawler;

import java.util.function.Consumer;

class DocumentContext extends ResourceContext {

    void select(String s, Consumer<ElementContext> r) {
        // todo
    }

    void select(String s, Runnable r) {
        select(s, elementContext -> {
            r.run();
        });
    }
}
