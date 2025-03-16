package com.sismics.reader.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.util.http.ReaderHttpClient;
import com.sismics.reader.core.dao.file.html.FeedChooserStrategy;
import com.sismics.reader.core.dao.file.html.RssExtractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;

public class RssUrlStrategy implements UrlStrategy {

    private static final Logger log = LoggerFactory.getLogger(RssUrlStrategy.class);

    @Override
    public RssReader getRssFeedReader(String url) {
        try {
            return parseFeedOrPage(url, true);
        } catch (Exception e) {
            log.error("Error while parsing feed: " + url, e);
            return null;
        }
    }

    private RssReader parseFeedOrPage(String url, boolean parsePage) throws Exception {
        try {
            final RssReader reader = new RssReader();
            new ReaderHttpClient() {

                @Override
                public Void process(InputStream is) throws Exception {
                    reader.readRssFeed(is);
                    return null;
                }
            }.open(new URL(url));
            reader.getFeed().setRssUrl(url);
            return reader;
        } catch (Exception eRss) {
            boolean recoverable = !(eRss instanceof UnknownHostException ||
                    eRss instanceof FileNotFoundException);
            if (parsePage && recoverable) {
                try {
                    final RssExtractor extractor = new RssExtractor(url);
                    new ReaderHttpClient() {

                        @Override
                        public Void process(InputStream is) throws Exception {
                            extractor.readPage(is);
                            return null;
                        }
                    }.open(new URL(url));
                    List<String> feedList = extractor.getFeedList();
                    if (feedList == null || feedList.isEmpty()) {
                        logParsingError(url, eRss);
                    }
                    String feed = new FeedChooserStrategy().guess(feedList);
                    return parseFeedOrPage(feed, false);
                } catch (Exception ePage) {
                    logParsingError(url, ePage);
                }
            } else {
                logParsingError(url, eRss);
            }

            throw eRss;
        }
    }

    private void logParsingError(String url, Exception e) {
        if (log.isWarnEnabled()) {
            if (e instanceof UnknownHostException ||
                    e instanceof FileNotFoundException ||
                    e instanceof ConnectException) {
                log.warn(MessageFormat.format("Error parsing HTML page at URL {0} : {1}", url, e.getMessage()));
            } else {
                log.warn(MessageFormat.format("Error parsing HTML page at URL {0}", url));
            }
        }
    }
}