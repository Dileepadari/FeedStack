package com.sismics.reader.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.async.*;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private ServiceManager serviceManager;

    private EventBusManager eventBusManager;

    // /**
    //  * Event bus.
    //  */
    // private EventBus eventBus;

    // /**
    //  * Generic asynchronous event bus.
    //  */
    // private EventBus asyncEventBus;

    // /**
    //  * Asynchronous event bus for emails.
    //  */
    // private EventBus mailEventBus;

    // /**
    //  * Asynchronous event bus for mass imports.
    //  */
    // private EventBus importEventBus;

    // /**
    //  * Feed service.
    //  */
    // private FeedService feedService;

    // /**
    //  * Indexing service.
    //  */
    // private IndexingService indexingService;

    // /**
    //  * Asynchronous executors.
    //  */
    // private List<ExecutorService> asyncExecutorList;

    /**
     * Private constructor.
     */
    private AppContext() {
        serviceManager = new ServiceManager();
        eventBusManager = new EventBusManager();
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

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public EventBusManager getEventBusManager() {
        return eventBusManager;
    }   
}
