package com.ilyamur.scrawler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import com.google.common.collect.Lists;
import com.ui4j.api.dom.Document;
import com.ui4j.api.dom.Element;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@RunWith(MockitoJUnitRunner.class)
public class DocumentContextTest {

    private static final String ANY_SELECTOR = StringUtils.EMPTY;

    private Document document;

    private DocumentContext target;
    private Element element;

    @Before
    public void before() {
        document = mock(Document.class);
        List<Element> elements = Lists.newArrayList();
        element = mock(Element.class);
        elements.add(element);
        doReturn(elements).when(document).queryAll(any());
        target = spy(new DocumentContext(document));
    }

    @Test
    public void selectConsumer_executes() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        target.select(ANY_SELECTOR, elementContext -> {
            atomicBoolean.set(true);
        });

        assertTrue("Select method should execute callback", atomicBoolean.get());
    }

    @Test
    public void selectConsumer_passesElementContext() {
        ElementContext mockElementContext = mock(ElementContext.class);
        doReturn(mockElementContext).when(target).createElementContext(element);

        target.select(ANY_SELECTOR, elementContext -> {
            assertEquals(mockElementContext, elementContext);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void selectRunnable_executes() {
        Runnable runnable = mock(Runnable.class);

        target.select(ANY_SELECTOR, runnable);

        verify(target).select(eq(ANY_SELECTOR), any(Consumer.class));
        verify(runnable).run();
    }
}
