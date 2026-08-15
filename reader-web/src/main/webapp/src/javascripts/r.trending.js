/**
 * Trending articles module.
 */
r.trending = {
    /**
     * Initialize trending articles module.
     */
    init: function() {
        console.log("Initializing trending articles module");
        
        // Target the subscriptions container which has the menu items
        var $subscriptions = $('#subscriptions');
        
        // Check if we already added trending section
        if ($('#trending-header').length === 0 && $subscriptions.length > 0) {
            // Find the "Latest" section's ul to add after it
            var $latestSection = $subscriptions.find('h1:contains("Latest")').next('ul');
            
            if ($latestSection.length > 0) {
                // Create section header and list similar to other sections
                var html = '<h1 id="trending-header">Top 5 Articles</h1>' +
                           '<ul id="trending-articles-list" class="trending-list"></ul>';
                
                // Add after the latest section
                $latestSection.after(html);
                console.log("Trending section added after Latest section");
            } else {
                console.error("Could not find Latest section to add trending articles");
            }
        }
        
        // Add CSS styles
        this.addStyles();
        
        // Load trending articles
        this.load();
        
        // Refresh trending periodically (every 5 minutes)
        setInterval(this.load.bind(this), 5 * 60 * 1000);
    },

    /**
     * Add CSS styles for trending articles.
     */
    addStyles: function() {
        // Only add styles if they don't exist yet
        if ($('#trending-styles').length === 0) {
            var css = `
                #trending-header {
                    margin-top: 15px;
                }
                #trending-articles-list {
                    list-style: none;
                    padding: 0;
                    margin: 0 0 15px 0;
                }
                #trending-articles-list li {
                    padding: 5px 0 5px 15px;
                    position: relative;
                }
                #trending-articles-list li a {
                    display: block;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    padding-right: 45px; /* Space for star count */
                }
                #trending-articles-list .trending-rank {
                    position: absolute;
                    left: 2px;
                    width: 14px;
                    height: 14px;
                    line-height: 14px;
                    text-align: center;
                    background: #337ab7;
                    color: white;
                    border-radius: 50%;
                    font-size: 10px;
                    font-weight: bold;
                }
                #trending-articles-list .trending-stars {
                    position: absolute;
                    right: 5px;
                    color: #f8ac59;
                    font-size: 10px;
                }
            `;
            
            $('head').append('<style id="trending-styles">' + css + '</style>');
        }
    },

    /**
     * Load trending articles.
     */
    load: function() {
        console.log("Loading trending articles...");
        var that = this;
        r.util.ajax({
            url: r.util.url.trending,
            type: 'GET',
            success: function(data) {
                console.log("Trending articles loaded:", data.articles);
                var articles = data.articles;
                var $list = $('#trending-articles-list');
                $list.empty();
                
                if (!articles || articles.length === 0) {
                    console.log("No trending articles found");
                    $list.append('<li class="empty">No trending articles yet</li>');
                    return;
                }
                
                $.each(articles, function(i, article) {
                    console.log("Adding trending #" + (i+1) + ": " + article.title);
                    var $item = $('<li>' +
                                 '<span class="trending-rank">' + (i + 1) + '</span>' +
                                 '<a href="' + article.url + '" target="_blank" title="' + article.title + '">' + 
                                 article.title + '</a>' +
                                 '<span class="trending-stars"><i class="fa fa-star"></i> ' + 
                                 article.star_count + '</span>' +
                                 '</li>');
                    $list.append($item);
                });
                
                // Make sure the trending section is visible
                $('#trending-header, #trending-articles-list').show();
            },
            error: function(xhr) {
                console.error("Error loading trending articles:", xhr);
                // Show error in the trending list
                $('#trending-articles-list').html('<li class="error">Failed to load trending articles</li>');
            }
        });
    },

    /**
     * Refresh trending articles after a star/unstar operation.
     */
    refreshAfterStar: function() {
        // Wait a bit to ensure the server has processed the star/unstar
        var that = this;
        setTimeout(function() {
            that.load();
        }, 300);
    }
};

// Remove the self-initialization code that was causing the conflict
// $(function() {
//   r.trending.init();
// }); 