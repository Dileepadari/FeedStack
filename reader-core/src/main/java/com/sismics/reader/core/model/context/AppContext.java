====FILE_DELIMITER====
package com.sismics.reader.core.model.context;

import java.util.concurrent.CountDownLatch;

/**
 * Global application context.
 *
 * @author jtremeaux
 */
public class AppContext {
    /**
     * Singleton instance.
     */
    private static AppContext instance;

    /**
     * Event bus.
     */
    private EventBus eventBus;

    /**
     * Async operations latch.
     */
    private CountDownLatch asyncLatch = new CountDownLatch(1);

    /**
     * Returns a single instance of the application context.
     *
     * @return Application context
     */
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    /**
     * Reset the event bus and async latch.
     */
    public void reset appContext() {
        // reset async latch
        asyncLatch = new CountDownLatch(1);

        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());
    }

    /**
     * Getter of eventBus.
     *
     * @return eventBus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    private AppContext() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());
    }
}
====FILE_DELIMITER====
package com.sismics.reader.core.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Service for indexing feeds.
 *
 * @author jtremeaux
 */
public class IndexingService {

    /**
     * Lucene storage directory.
     */
    private String luceneStorage;

    /**
     * Application context
     */
    private AppContext appContext;

    /**
     * Async operations latch.
     */
    private CountDownLatch asyncLatch;

    /**
     * Constructor.
     *
     * @param luceneStorage Lucene storage directory
     * @param appContext Application context
     */
    public IndexingService(String luceneStorage, AppContext appContext) {
        this.luceneStorage = luceneStorage;
        this.appContext = appContext;
    }

    /**
     * Start the indexing service.
     */
    public void start() {
        if (luceneStorage == null) {
            ConfigDao configDao = new ConfigDao();
            Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
            luceneStorage = luceneStorageConfig != null ? luceneStorageConfig.getValue() : null;
        }
        if (luceneStorage == null) {
            throw new IllegalStateException("Lucene storage directory is not configured");
        }
        if (EnvironmentUtil.isUnitTest()) {
            executeAsync();
            return;
        }
        executeAsync();
    }

    /**
     * Start and wait the indexing service.
     */
    public void startAndWait() {
        start();
        try {
            join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stop the indexing service.
     */
    public void stopAndWait() {
        if (!EnvironmentUtil.isUnitTest()) {
            waitForAsync();
        }
    }

    /**
     * Start asynchronous operations.
     */
    private void executeAsync() {
        try {
            // create async event bus
            appContext.getEventBus().register(newAsyncEventBus());
        } finally {
            if (!EnvironmentUtil.isUnitTest()) {
                waitForAsync();
            }
        }
    }

    /**
     * Wait for termination of all asynchronous events.
     * /!\ Must be used only in unit tests and never a multi-user environment.
     */
    public void waitForAsync() {
        try {
            if (asyncLatch.getCount() > 0) {
                asyncLatch.await(60, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            // NOP
        }
    }

    /**
     * Creates a new asynchronous event bus.
     *
     * @return Async event bus
     */
    private EventBus newAsyncEventBus() {
        if (EnvironmentUtil.isUnitTest()) {
            return new EventBus();
        } else {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            EventBus asyncEventBus = new EventBus(executor);
            
            // use CountDownLatch for async operations
            asyncLatch = new CountDownLatch(1);
            asyncEventBus.register(new AsyncEventListener(asyncLatch));
            
            return asyncEventBus;
        }
    }

    /**
     * Getter for luceneStorage.
     *
     * @return Lucene storage directory
     */
    public String getLuceneStorage() {
        return luceneStorage;
    }

    /**
     * Setter for luceneStorage.
     *
     * @param luceneStorage Lucene storage directory
     */
    public void setLuceneStorage(String luceneStorage) {
        this.luceneStorage = luceneStorage;
    }
}