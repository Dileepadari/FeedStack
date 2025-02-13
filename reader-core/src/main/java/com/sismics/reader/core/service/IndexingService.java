```java
package com.sismics.reader.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.reader.core.dao.file.html.FeedChooserStrategy;
import com.sismics.reader.core.dao.file.html.RssExtractor;
import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.dao.jpa.*;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.event.ArticleCreatedAsyncEvent;
import com.sismics.reader.core.event.ArticleDeletedAsyncEvent;
import com.sismics.reader.core.event.ArticleUpdatedAsyncEvent;
import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.mediator.Mediator;
import com.sismics.reader.core.model.jpa.*;
import com.sismics.reader.core.util.EntityManagerUtil;
import com.sismics.reader.core.util.TransactionUtil;
import com.sismics.reader.core.util.http.ReaderHttpClient;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.core.util.sanitizer.ArticleSanitizer;
import com.sismics.reader.core.util.sanitizer.TextSanitizer;
import com.sismics.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Feed service.
 *
 * @author jtremeaux 
 */
public class FeedService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FeedService.class);

    private Mediator mediator;

    public FeedService(Mediator mediator) {
        this.mediator = mediator;
    }


    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
    }

    @Override
    protected void runOneIteration() {
        // Don't let Guava manage our exceptions, or they will be swallowed and the service will silently stop
        try {
            TransactionUtil.handle(() -> synchronizeAllFeeds());
        } catch (Throwable t) {
            log.error("Error synchronizing feeds", t);
        }
    }

    @Override
    protected Scheduler scheduler() {
        // TODO Implement a better schedule strategy... Use update period specified in the feed if avail & use last update date from feed to backoff
        return Scheduler.newFixedDelaySchedule(0, 10, TimeUnit.MINUTES);
    }

    /**
     * Synchronize all feeds.
     */
    public void synchronizeAllFeeds() {
        // Update all feeds currently having subscribed users
        FeedDao feedDao = new FeedDao();
        FeedCriteria feedCriteria = new FeedCriteria()
                .setWithUserSubscription(true);
        List<FeedDto> feedList = feedDao.findByCriteria(feedCriteria);
        List<FeedSynchronization> feedSynchronizationList = new ArrayList<FeedSynchronization>();
        for (FeedDto feed : feedList) {
            FeedSynchronization feedSynchronization = new FeedSynchronization();
            feedSynchronization.setFeedId(feed.getId());
            feedSynchronization.setSuccess(true);
            long startTime = System.currentTimeMillis();

            try {
                synchronize(feed.getRssUrl());
            } catch (Exception e) {
                log.error(MessageFormat.format("Error synchronizing feed at URL: {0}", feed.getRssUrl()), e);
                feedSynchronization.setSuccess(false);
                feedSynchronization.setMessage(ExceptionUtils.getStackTrace(e));
            }
            feedSynchronization.setDuration((int) (System.currentTimeMillis() - startTime));
            feedSynchronizationList.add(feedSynchronization);
            TransactionUtil.commit();
        }

        // If all feeds have failed, then we infer that the network is probably down
        FeedSynchronizationDao feedSynchronizationDao = new FeedSynchronizationDao();
        boolean networkDown = true;
        for (FeedSynchronization feedSynchronization : feedSynchronizationList) {
            if (feedSynchronization.isSuccess()) {
                networkDown = false;
                break;
            }
        }

        // Update the status of all synchronized feeds
        if (!networkDown) {
            for (FeedSynchronization feedSynchronization : feedSynchronizationList) {
                feedSynchronizationDao.create(feedSynchronization);
                feedSynchronizationDao.deleteOldFeedSynchronization(feedSynchronization.getFeedId(), 600);
            }
            TransactionUtil.commit();
        }
    }

    /**
     * Synchronize the feed to local database.
     *
     * @param url RSS url of a feed or page containing a feed to synchronize
     */
    public Feed synchronize(String url) throws Exception {
        long startTime = System.currentTimeMillis();

        // Parse the feed
        RssReader rssReader = parseFeedOrPage(url, true);
        Feed newFeed = rssReader.getFeed();
        List<Article> articleList = rssReader.getArticleList();

        completeArticleList(articleList);

        // Get articles that were removed from RSS compared to last fetch
        List<Article> articleToRemove = getArticleToRemove(articleList);
        if (!articleToRemove.isEmpty()) {
            for (Article article : articleToRemove) {
                // Update unread counts
                // FIXME count be optimized in 1 query instead of a*s*2
                List<UserArticleDto> userArticleDtoList = new UserArticleDao()
                        .findByCriteria(new UserArticleCriteria()
                                .setArticleId(article.getId())
                                .setFetchAllFeedSubscription(true) // to test: subscribe another user, u2, read u1, not u2, u1 is decremented anyway
                                .setUnread(true));

                for (UserArticleDto userArticleDto : userArticleDtoList) {
                    FeedSubscriptionDto feedSubscriptionDto = new FeedSubscriptionDao().findFirstByCriteria(new FeedSubscriptionCriteria()
                            .setId(userArticleDto.getFeedSubscriptionId()));
                    if (feedSubscriptionDto != null) {
                        new FeedSubscriptionDao().updateUnreadCount(feedSubscriptionDto.getId(), feedSubscriptionDto.getUnreadUserArticleCount() - 1);
                    }
                }
            }

            // Delete articles that don't exist anymore
            for (Article article: articleToRemove) {
                new ArticleDao().delete(article.getId());
            }

            // Removed articles from index
            ArticleDeletedAsyncEvent articleDeletedAsyncEvent = new ArticleDeletedAsyncEvent();
            articleDeletedAsyncEvent.setArticleList(articleToRemove);
            mediator.notify(this, articleDeletedAsyncEvent);
        }

        // Create the feed if necessary (not created and currently in use by another user)
        FeedDao feedDao = new FeedDao();
        String rssUrl = newFeed.getRssUrl();
        Feed feed = feedDao.getByRssUrl(rssUrl);
        if (feed == null) {
            feed = new Feed();
            feed.setUrl(newFeed.getUrl());
            feed.setBaseUri(newFeed.getBaseUri());
            feed.setRssUrl(rssUrl);
            feed.setTitle(StringUtils.abbreviate(newFeed.getTitle(), 100));
            feed.setLanguage(newFeed.