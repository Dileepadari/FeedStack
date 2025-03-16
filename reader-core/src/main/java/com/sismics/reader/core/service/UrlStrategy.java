package com.sismics.reader.core.service;

import com.sismics.reader.core.dao.file.rss.RssReader;

public interface UrlStrategy {
    RssReader getRssFeedReader(String url);
}