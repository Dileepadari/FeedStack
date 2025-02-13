====FILE_DELIMITER====
package com.sismics.reader.core.model.context;

import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.mediator.ConcreteMediator;
import com.sismics.reader.core.mediator.Mediator;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

import java.util.concurrent.ThreadPoolExecutor;

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
     * Feed service.
     */
    private FeedService feedService;

    /**
     * Indexing service.
     */
    private IndexingService indexingService;

    /**
     * Mediator
     */
    private Mediator mediator;

    /**
     * Private constructor.
     */
    private AppContext() {
        resetEventBus();

        mediator = new ConcreteMediator(this, feedService);

        feedService = new FeedService(mediator);
        feedService.startAndWait();

        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
        indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null, mediator);
        indexingService.startAndWait();
    }

    /**
     * (Re)-initializes the event buses.
     */
    private void resetEventBus() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());
    }

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
     * Wait for termination of all asynchronous events.
     * /!\ Must be used only in unit tests and never a multi-user environment.
     */
    public void waitForAsync() {
        if (EnvironmentUtil.isUnitTest()) {
            return;
        }
        try {
            // Shutdown executor, don't accept any more tasks (can cause error with nested events)
            try {
                ((ThreadPoolExecutor) eventBus).shutdown();
                eventBus.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // NOP
            }
        } finally {
            resetEventBus();
        }
    }

    /**
     * Getter of eventBus.
     *
     * @return eventBus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Getter of feedService.
     *
     * @return feedService
     */
    public FeedService getFeedService() {
        return feedService;
    }

    /**
     * Getter of indexingService.
     *
     * @return indexingService
     */
    public IndexingService getIndexingService() {
        return indexingService;
    }
}

====FILE_DELIMITER====
package com.sismics.reader.core.service;

import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.async.*;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.util.EnvironmentUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Service for indexing feeds.
 *
 * @author jtremeaux
 */
public class IndexingService extends Thread {
    /**
     * Lucene storage directory.
     */
    private String luceneStorage;

    /**
     * Application context
     */
    private AppContext appContext;

    /**
     * Asynchronous event bus.
     */
    private EventBus eventBus;

    /**
     * Constructor.
     *
     * @param luceneStorage Lucene storage directory
     */
    public IndexingService(String luceneStorage, AppContext appContext) {
        this.luceneStorage = luceneStorage;
        this.appContext = appContext;
    }

    /**
     * Execute asynchronous operations.
     */
    private void executeAsync() {
        try {
            eventBus = newAsyncEventBus();
            eventBus.register(new ArticleCreatedAsyncListener());
            eventBus.register(new ArticleUpdatedAsyncListener());
            eventBus.register(new ArticleDeletedAsyncListener());
            eventBus.register(new RebuildIndexAsyncListener());
            eventBus.register(new FaviconUpdateRequestedAsyncListener());
            
            start();
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
        if (EnvironmentUtil.isUnitTest()) {
            return;
        }
        try {
            // Shutdown executor, don't accept any more tasks (can cause error with nested events)
            try {
                ((ThreadPoolExecutor) eventBus).shutdown();
                eventBus.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // NOP
            }
        } finally {
            eventBus = null;
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
            return new com.google.common.eventbus.AsyncEventBus(executor);
        }
    }

    /**
     * Start the thread.
     */
    @Override
    public void run() {
        reloadIndex();
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
        if (!EnvironmentUtil.isUnitTest() && eventBus != null) {
            waitForAsync();
        }
    }

    /**
     * Reload the index.
     */
    public void reloadIndex() {
        appContext.getFeedService().reloadIndex();
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