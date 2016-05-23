package com.ilyamur.scrawler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.Page;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@RunWith(Enclosed.class)
public class CrawlerTest {

    private static final String ANY_URI = StringUtils.EMPTY;

    @RunWith(MockitoJUnitRunner.class)
    public static class CrawlerTest_nonConfigured {

        @Test(expected = NullPointerException.class)
        public void build_whenMissingConfiguration_throwsConfigurationError() {
            (new Crawler.Builder())
                    .build();
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class CrawlerTest_configured {

        private final CurrentThreadExecutor currentThreadExecutor = spy(new CurrentThreadExecutor());

        @Mock
        private BrowserEngine browserEngine;

        private Crawler target;

        @Before
        public void before() {
            Crawler crawler = (new Crawler.Builder())
                    .setExecutionService(currentThreadExecutor)
                    .build();
            target = spy(crawler);
            prepareBrowserEngine();
        }

        private void prepareBrowserEngine() {
            doReturn(browserEngine).when(target).getWebKit();
            Page page = mock(Page.class);
            doReturn(page).when(browserEngine).navigate(ANY_URI);
        }

        @Test
        public void getConsumer_executes() {
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);

            target.get(ANY_URI, documentContext -> {
                atomicBoolean.set(true);
            });

            assertTrue("Get method should execute callback", atomicBoolean.get());
        }

        @Test
        @SuppressWarnings("unchecked")
        public void getRunnable_executes() {
            Runnable runnable = mock(Runnable.class);

            target.get(ANY_URI, runnable);

            verify(target).get(eq(ANY_URI), any(Consumer.class));
            verify(runnable).run();
        }

        @Test
        public void shutdown() {

            target.get(ANY_URI, documentContext -> {
            });
            target.shutdown();

            verify(currentThreadExecutor).shutdown();
            verify(browserEngine).shutdown();
        }

        @Test
        public void getDefault() {
            Crawler crawler = Crawler.getDefault();

            assertNotNull(crawler.executionService);
        }
    }
}
