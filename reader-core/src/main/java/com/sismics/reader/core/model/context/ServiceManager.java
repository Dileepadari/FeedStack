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


public class ServiceManager {
    private FeedService feedService;
    private IndexingService indexingService;

    public ServiceManager() {

        feedService = new FeedService();
        feedService.startAndWait();

        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
        indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null);
        indexingService.startAndWait();
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