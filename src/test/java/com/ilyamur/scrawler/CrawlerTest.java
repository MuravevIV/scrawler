package com.ilyamur.scrawler;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@RunWith(Enclosed.class)
public class CrawlerTest {

    private static final String ANY_URI = StringUtils.EMPTY;

    private static final CurrentThreadExecutor CURRENT_THREAD_EXECUTOR = new CurrentThreadExecutor();

    @RunWith(MockitoJUnitRunner.class)
    public static class CrawlerTest_nonConfigured {

        @Spy
        private Crawler target;

        @Test(expected = NullPointerException.class)
        public void get_whenNonConfigured_throwsConfigurationError() {

            target.get(ANY_URI, documentContext -> {
            });
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class CrawlerTest_configured {

        @Spy
        private Crawler target;

        @Before
        public void before() {
            prepareConfiguration();
        }

        private void prepareConfiguration() {
            CrawlerConfiguration crawlerConfiguration = mock(CrawlerConfiguration.class);
            when(crawlerConfiguration.getExecutionService()).thenReturn(CURRENT_THREAD_EXECUTOR);
            target.setConfiguration(crawlerConfiguration);
        }

        @Test
        public void getConsumable_executes() {
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);

            target.get(ANY_URI, documentContext -> {
                atomicBoolean.set(true);
            });

            assertTrue("Get method should execute", atomicBoolean.get());
        }

        @Test
        @SuppressWarnings("unchecked")
        public void getRunnable_executes() {
            Runnable runnable = mock(Runnable.class);

            target.get(ANY_URI, runnable);

            verify(target).get(eq(ANY_URI), any(Consumer.class));
            verify(runnable).run();
        }
    }
}
