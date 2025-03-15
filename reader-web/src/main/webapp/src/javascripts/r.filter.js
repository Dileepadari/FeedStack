r.filter = r.filter || {};

r.filter.init = function() {
    // Initialize filter state
    r.filter.activeFilters = {
        sources: [],
        categories: []
    };

    // Initialize the filter button tooltip
    $('#toolbar > .filter-button').qtip({
        content: { text: $('#qtip-filter') },
        position: {
            my: 'top right',
            at: 'bottom center',
            effect: false,
            viewport: $(window),
            adjust: { method: 'shift' }
        },
        show: { event: 'click' },
        hide: { event: 'click unfocus' },
        style: { classes: 'qtip-light qtip-shadow' },
        events: {
            show: function() {
                r.filter.loadFilters();
            }
        }
    });

    // Handle filter apply button click
    $('#qtip-filter').on('click', '.filter-apply-button', function() {
        const selectedSources = [];
        const selectedCategories = [];

        $('.sources-list input:checked').each(function() {
            selectedSources.push($(this).val());
        });

        $('.categories-list input:checked').each(function() {
            selectedCategories.push($(this).val());
        });

        // Update active filters
        r.filter.activeFilters = {
            sources: selectedSources,
            categories: selectedCategories
        };

        // Apply the filters
        r.filter.applyFilters();

        // Hide the filter dropdown
        $('#toolbar > .filter-button').qtip('hide');

        // Update filter button state
        r.filter.updateFilterButtonState();
    });

    // Initialize clear filters button
    $('#toolbar').on('click', '.clear-filters-button', function(e) {
        e.preventDefault();
        r.filter.clearFilters();
    });
};

r.filter.loadFilters = function() {
    // Load subscriptions and categories
    r.util.ajax({
        url: r.util.url.subscription_list,
        type: 'GET',
        done: function(data) {
            const sourcesHtml = [];
            const categoriesHtml = [];

            // Add sources
            if (data.categories && data.categories[0]) {
                const rootCategory = data.categories[0];

                // Process root subscriptions
                if (rootCategory.subscriptions) {
                    rootCategory.subscriptions.forEach(function(subscription) {
                        sourcesHtml.push(
                            `<div class="filter-item">
                                <input type="checkbox" value="${subscription.id}" id="source-${subscription.id}"
                                    ${r.filter.activeFilters.sources.includes(subscription.id) ? 'checked' : ''}>
                                <label for="source-${subscription.id}">${r.util.escape(subscription.title)}</label>
                            </div>`
                        );
                    });
                }

                // Process categories and their subscriptions
                if (rootCategory.categories) {
                    rootCategory.categories.forEach(function(category) {
                        categoriesHtml.push(
                            `<div class="filter-item">
                                <input type="checkbox" value="${category.id}" id="category-${category.id}"
                                    ${r.filter.activeFilters.categories.includes(category.id) ? 'checked' : ''}>
                                <label for="category-${category.id}">${r.util.escape(category.name)}</label>
                            </div>`
                        );

                        // Add subscriptions from this category
                        if (category.subscriptions) {
                            category.subscriptions.forEach(function(subscription) {
                                sourcesHtml.push(
                                    `<div class="filter-item">
                                        <input type="checkbox" value="${subscription.id}" id="source-${subscription.id}"
                                            ${r.filter.activeFilters.sources.includes(subscription.id) ? 'checked' : ''}>
                                        <label for="source-${subscription.id}">${r.util.escape(subscription.title)}</label>
                                    </div>`
                                );
                            });
                        }
                    });
                }
            }

            $('.sources-list').html(sourcesHtml.join(''));
            $('.categories-list').html(categoriesHtml.join(''));
        }
    });
};

r.filter.isUnreadMode = function(){
       var currentUrl = window.location.hash;
       return currentUrl.indexOf('/feed/unread') !== -1;
 }

r.filter.isStarredMode = function(){
    var currentUrl = window.location.hash;
    return currentUrl.indexOf('/feed/starred') !== -1;
}

r.filter.applyFilters = function() {
    // Get current unread state
    const unread = r.filter.isUnreadMode();
    const starred = r.filter.isStarredMode();

    // Build URL parameters
    let params = [];

    // Add source filters
    r.filter.activeFilters.sources.forEach(function(sourceId) {
        params.push('source=' + encodeURIComponent(sourceId));
    });

    // Add category filters
    r.filter.activeFilters.categories.forEach(function(categoryId) {
        params.push('category=' + encodeURIComponent(categoryId));
    });

    // Add unread parameter if needed
    if (unread) {
        params.push('filter=unread');
    }

    else if (starred) {
        params.push('filter=starred');
    }
    else {
        params.push('filter=all');
    }

    // Fetch filtered articles
    r.util.ajax({
        url: r.util.url.filter + (params.length ? '?' + params.join('&') : ''),
        type: 'GET',
        done: function(data) {
            // Clear current feed
            r.feed.cache.container.empty();

            // Process and display articles
            if (data.articles && data.articles.length > 0) {
                // Update filter indicator
                if (r.filter.activeFilters.categories.length > 0 || r.filter.activeFilters.sources.length > 0) {
                    $('#filter-button').addClass('active-filter');
                } else {
                    $('#filter-button').removeClass('active-filter');
                }

                // Add bumper
                var bumper = r.feed.buildBumper(data);
                r.feed.context.bumper = bumper;
                r.feed.cache.container.append(bumper);

                // Building articles
                $(data.articles).each(function(i, article) {
                    // Escape some fields
                    article.subscription.title = r.util.escape(article.subscription.title);
                    article.creator = r.util.escape(article.creator);

                    // Build article
                    var item = r.article.build(article);
                    r.feed.context.bumper.before(item);

                    // Store last item
                    if(i == data.articles.length - 1) {
                        r.feed.context.lastItem = item;
                    }
                });

                // Focus article list and redraw
                r.feed.cache.container
                    .trigger('focus')
                    .redraw();

                // Setup pagination for filtered articles
                r.feed.context.url = r.util.url.filter + (params.length ? '?' + params.join('&') : '');
                r.feed.context.fullyLoaded = data.articles.length < r.feed.context.limit();

                // Trigger paging in case all articles are visible
                r.feed.triggerPaging();
            } else {
                // Show no results message
                r.feed.cache.container.html(
                    '<div class="no-results">' +
                    '<p>' + $.t('filter.no_results') + '</p>' +
                    '</div>'
                );
            }
        }
    });
};

r.filter.clearFilters = function() {
    // Reset active filters
    r.filter.activeFilters = {
        sources: [],
        categories: []
    };

    // Update filter button state
    r.filter.updateFilterButtonState();

    // Reload default feed
    if (r.filter.isUnreadMode()) {
        window.location.href = '#/feed/unread';
        
    } else if (r.filter.isStarredMode()) {
        window.location.href = '#/feed/starred';
    } else {
        window.location.href = '#/feed/all';
    }
};

r.filter.updateFilterButtonState = function() {
    const hasActiveFilters =
        r.filter.activeFilters.sources.length > 0 ||
        r.filter.activeFilters.categories.length > 0;

    // Update filter button appearance
    if (hasActiveFilters) {
        $('#toolbar > .filter-button').addClass('active');

        // Add clear filters button if it doesn't exist
        if ($('#toolbar > .clear-filters-button').length === 0) {
            $('#toolbar').append(
                '<button class="clear-filters-button" title="Clear filters">' +
                '<i class="fas fa-times"></i>Clear filters</button>'
            );
        }
    } else {
        $('#toolbar > .filter-button').removeClass('active');
        $('#toolbar > .clear-filters-button').remove();
    }
};