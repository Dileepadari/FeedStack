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
    const unread = r.filter.isUnreadMode();
    const starred = r.filter.isStarredMode();
    let params = [];

    // Add source and category filters
    r.filter.activeFilters.sources.forEach(sourceId => params.push('source=' + encodeURIComponent(sourceId)));
    r.filter.activeFilters.categories.forEach(categoryId => params.push('category=' + encodeURIComponent(categoryId)));

    // Add unread/starred/all filter
    if (unread) {
        params.push('filter=unread');
    } else if (starred) {
        params.push('filter=starred');
    } else {
    }
    params.push('filter=all');
    r.feed.cache.container.empty();
    r.feed.context.url = r.util.url.filter + (params.length ? '?' + params.join('&') : '');
    // Reload filtered feed
    $('#filter-button').toggleClass('active-filter', r.filter.activeFilters.sources.length > 0 || r.filter.activeFilters.categories.length > 0);
    r.feed.context.lastItem = null;
    r.feed.load();

    // // Fetch filtered articles
    // r.util.ajax({
    //     url: r.util.url.filter + (params.length ? '?' + params.join('&') : ''),
    //     type: 'GET',
    //     done: function(data) {
    //         r.feed.cache.container.empty();

    //         if (data.articles && data.articles.length > 0) {
    //             // Update filter indicator and pagination context
    //             $('#filter-button').toggleClass('active-filter', r.filter.activeFilters.sources.length > 0 || r.filter.activeFilters.categories.length > 0);

    //             // Build articles
    //             $(data.articles).each((i, article) => {
    //                 article.subscription.title = r.util.escape(article.subscription.title);
    //                 article.creator = r.util.escape(article.creator);

    //                 const item = r.article.build(article);
    //                 r.feed.context.bumper.before(item);

    //                 if (i === data.articles.length - 1) {
    //                     r.feed.context.lastItem = item;
    //                 }
    //             });

    //             // Update pagination context
    //             r.feed.context.url = r.util.url.filter + (params.length ? '?' + params.join('&') : '');
    //             r.feed.context.fullyLoaded = data.articles.length < r.feed.context.limit();

    //             // Trigger paging
    //             r.feed.triggerPaging();
    //         } else {
    //             r.feed.cache.container.html('<div class="no-results"><p>' + $.t('filter.no_results') + '</p></div>');
    //         }
    //     }
    // });
};

r.filter.clearFilters = function() {
    // Reset active filters
    r.filter.activeFilters = {
        sources: [],
        categories: []
    };

    // Update filter button state
    r.filter.updateFilterButtonState();
    // reset context url
    if(r.filter.isUnreadMode()){
        r.feed.context.url = r.util.url.unread;
    }
    else if(r.filter.isStarredMode()){
        r.feed.context.url = r.util.url.starred;
    }else{
        r.feed.context.url = r.util.url.all;
    }
    // Reload feed
    r.feed.load();
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