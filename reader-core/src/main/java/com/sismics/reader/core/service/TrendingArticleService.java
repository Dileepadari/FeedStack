package com.sismics.reader.core.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service to manage trending articles.
 * 
 * @author [your name]
 */
public class TrendingArticleService {
    private static final Logger log = LoggerFactory.getLogger(TrendingArticleService.class);
    
    /**
     * Maximum number of trending articles to track.
     */
    private static final int MAX_TRENDING_ARTICLES = 5;
    
    /**
     * Cache of trending articles, sorted by star count (descending).
     */
    private final List<ArticleDto> trendingArticles = new ArrayList<>(MAX_TRENDING_ARTICLES);
    
    /**
     * Lock for thread safety when updating trending articles.
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * Singleton instance.
     */
    private static TrendingArticleService instance;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private TrendingArticleService() {
        // Initialize trending articles list
        refreshTrendingArticles();
    }
    
    /**
     * Get singleton instance.
     * 
     * @return Singleton instance
     */
    public static synchronized TrendingArticleService getInstance() {
        if (instance == null) {
            instance = new TrendingArticleService();
        }
        return instance;
    }
    
    /**
     * Refresh trending articles from database.
     */
    public void refreshTrendingArticles() {
        try {
            TransactionUtil.handle(() -> {
                ArticleDao articleDao = new ArticleDao();
                List<ArticleDto> allArticles = articleDao.findMostStarred(MAX_TRENDING_ARTICLES);
                
                lock.writeLock().lock();
                try {
                    trendingArticles.clear();
                    trendingArticles.addAll(allArticles);
                } finally {
                    lock.writeLock().unlock();
                }
            });
        } catch (Exception e) {
            log.error("Error refreshing trending articles", e);
        }
    }
    
    /**
     * Update trending articles when an article's star count changes.
     * 
     * @param articleId Article ID
     * @param newStarCount New star count
     */
    public void updateArticleStarCount(String articleId, int newStarCount) {
        try {
            TransactionUtil.handle(() -> {
                ArticleDao articleDao = new ArticleDao();
                ArticleDto updatedArticle = articleDao.findById(articleId);
                if (updatedArticle == null) {
                    return;
                }
                
                lock.writeLock().lock();
                try {
                    // Update star count in the updated article
                    updatedArticle.setStarCount(newStarCount);
                    
                    // Check if article is already in trending list
                    boolean articleFound = false;
                    for (int i = 0; i < trendingArticles.size(); i++) {
                        if (trendingArticles.get(i).getId().equals(articleId)) {
                            // Update the article in place
                            trendingArticles.set(i, updatedArticle);
                            articleFound = true;
                            break;
                        }
                    }
                    
                    // If article not in list but should be (has more stars than least trending)
                    if (!articleFound && (trendingArticles.size() < MAX_TRENDING_ARTICLES || 
                            (trendingArticles.size() > 0 && 
                             newStarCount > trendingArticles.get(trendingArticles.size() - 1).getStarCount()))) {
                        // Add the article to the list
                        if (trendingArticles.size() >= MAX_TRENDING_ARTICLES) {
                            // Remove the least trending article
                            trendingArticles.remove(trendingArticles.size() - 1);
                        }
                        trendingArticles.add(updatedArticle);
                    }
                    
                    // Re-sort the list
                    Collections.sort(trendingArticles, 
                            Comparator.comparing(ArticleDto::getStarCount).reversed());
                } finally {
                    lock.writeLock().unlock();
                }
            });
        } catch (Exception e) {
            log.error("Error updating trending articles", e);
        }
    }
    
    /**
     * Get the current trending articles.
     * 
     * @return List of trending articles
     */
    public List<ArticleDto> getTrendingArticles() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(trendingArticles);
        } finally {
            lock.readLock().unlock();
        }
    }
} 